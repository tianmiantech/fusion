/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.fusion.core.algorithm.rsa_psi.phase;

import cn.hutool.core.codec.Base64;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.algorithm.base.phase_action.AbstractIntersectionAction;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiRecord;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.util.PsiUtils;
import com.welab.wefe.common.BatchConsumer;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.CloseableUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P4IntersectionAction extends AbstractIntersectionAction<RsaPsiJob> {
    /**
     * E
     */
    private BigInteger publicExponent;
    /**
     * N
     */
    private BigInteger publicModulus;
    /**
     * 交集数据量
     */
    private LongAdder fruitCount = new LongAdder();
    private BufferedWriter writerForOnlyKey;

    public P4IntersectionAction(RsaPsiJob job) {
        super(job);
    }


    @Override
    protected void doAction() throws Exception {

        this.publicModulus = job.getPartner().psiBloomFilter.rsaPsiParam.publicModulus;
        this.publicExponent = job.getPartner().psiBloomFilter.rsaPsiParam.publicExponent;

        this.writerForOnlyKey = super.initResultFileOnlyKey();

        // 求交
        intersection();

        job.getJobResult().fusionCount = fruitCount.longValue();
        phaseProgress.setMessageAndLog("求交完毕，交集：" + fruitCount);
    }

    /**
     * 求交
     *
     * @throws StatusCodeWithException
     */
    private void intersection() throws Exception {
        LongAdder progress = new LongAdder();
        /**
         * 批处理
         * 这里 5 万数据加密要 20 秒（使用了并行处理，测试数据来自华为笔记本）
         * 建议这里设置 5 秒左右一批，不要太大，也不要太小。
         */
        BatchConsumer<RsaPsiRecord> consumer = new BatchConsumer<>(10_000, 5_000, records -> {
            try {
                List<RsaPsiRecord> fruits = matchOneBatch(records);

                saveFruits(fruits);

                // 更新进度
                fruitCount.add(fruits.size());
                progress.add(records.size());
                phaseProgress.updateCompletedWorkload(progress.longValue());
                phaseProgress.setMessage("已找到交集：" + fruitCount);
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                job.finishJobOnException(e);
            }
        });

        // 从数据源逐条读取数据集并编码
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(job.getJobTempData().allOriginalData);
        reader.readRows((index, row) -> {

            RsaPsiRecord record = new RsaPsiRecord();
            record.row = row;

            BigInteger blindFactor = PsiUtils.generateBlindingFactor(publicModulus);
            BigInteger random = blindFactor.modPow(publicExponent, publicModulus);
            record.inv = blindFactor.modInverse(publicModulus);

            String key = job.getMyself().dataResourceInfo.hashConfig.hash(row);
            BigInteger h = PsiUtils.stringToBigInteger(key);
            BigInteger x = h.multiply(random).mod(publicModulus);
            byte[] bytes = PsiUtils.bigIntegerToBytes(x);
            record.encodedKey = Base64.encode(bytes);

            consumer.add(record);
        });

        // 等待消费完成
        consumer.waitForFinishAndClose();
    }

    /**
     * 将结果写入文件
     */
    private void saveFruits(List<RsaPsiRecord> fruits) throws IOException {
        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;
        for (RsaPsiRecord record : fruits) {
            this.writerForOnlyKey.write(
                    hashConfig.hash(record.row) + System.lineSeparator()
            );
        }
    }

    /**
     * 对一批数据求交
     */
    private List<RsaPsiRecord> matchOneBatch(List<RsaPsiRecord> records) throws Exception {
        // 将数据发送到过滤器方加密
        List<String> encryptedList = job.getJobFunctions().encryptRsaPsiRecordsFunction.encrypt(
                job.getJobId(),
                job.getPartner().memberId,
                records.stream().map(x -> x.encodedKey).collect(Collectors.toList())
        );

        // 碰撞，并获取交集。
        List<RsaPsiRecord> fruits = PsiUtils.match(
                job.getPartner().psiBloomFilter,
                records,
                encryptedList,
                publicModulus
        );
        return fruits;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.Intersection;
    }

    @Override
    public long getTotalWorkload() {
        return job.getMyself().dataResourceInfo.dataCount;
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == JobRole.follower;
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.closeQuietly(this.writerForOnlyKey);
    }
}
