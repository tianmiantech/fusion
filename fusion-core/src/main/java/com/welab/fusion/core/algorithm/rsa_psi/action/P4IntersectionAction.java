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
package com.welab.fusion.core.algorithm.rsa_psi.action;

import cn.hutool.core.codec.Base64;
import com.welab.fusion.core.Job.FusionResult;
import com.welab.fusion.core.Job.JobRole;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiRecord;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.PsiUtils;
import com.welab.wefe.common.BatchConsumer;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P4IntersectionAction extends AbstractJobPhaseAction<RsaPsiJob> {
    private static final int batchSize = 50000;
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
    private BufferedWriter writerForOnlyIds;
    private LinkedHashSet<String> csvHeaderOnlyIds;
    private BufferedWriter writerForWithAdditionalColumns;
    private LinkedHashSet<String> csvHeaderWithAdditionalColumns;

    public P4IntersectionAction(RsaPsiJob job) {
        super(job);
    }


    @Override
    protected void doAction() throws Exception {

        publicModulus = job.getPartner().psiBloomFilter.rsaPsiParam.publicModulus;
        publicExponent = job.getPartner().psiBloomFilter.rsaPsiParam.publicExponent;

        FusionResult result = job.getJobResult();

        // 初始化结果文件
        result.resultFileOnlyIds = initResultFileOnlyIds();
        result.resultFileWithAdditionalColumns = initResultFileWithAdditionalColumns();

        // 求交
        intersection();

        // 释放资源
        this.writerForOnlyIds.close();
        this.writerForWithAdditionalColumns.close();

        result.finish(fruitCount.longValue());
    }

    /**
     * 初始化结果文件：包含附加列
     */
    private File initResultFileWithAdditionalColumns() throws Exception {
        this.csvHeaderWithAdditionalColumns = super.getResultFileCsvHeaderWithAdditionalColumns();

        // 初始化结果文件
        File file = FileSystem.FusionResult.getFileWithAdditionalColumns(job.getJobId());
        this.writerForWithAdditionalColumns = FileUtil.buildBufferedWriter(file, false);
        this.writerForWithAdditionalColumns.write(
                StringUtil.joinByComma(csvHeaderWithAdditionalColumns) + System.lineSeparator()
        );

        return file;
    }

    /**
     * 初始化结果文件：仅包含 id 列
     */
    private File initResultFileOnlyIds() throws Exception {

        this.csvHeaderOnlyIds = super.getResultFileCsvHeaderOnlyIds();

        File file = FileSystem.FusionResult.getFileOnlyIds(job.getJobId());
        this.writerForOnlyIds = FileUtil.buildBufferedWriter(file, false);
        this.writerForOnlyIds.write(
                StringUtil.joinByComma(csvHeaderOnlyIds) + System.lineSeparator()
        );

        return file;
    }

    /**
     * 求交
     *
     * @throws StatusCodeWithException
     */
    private void intersection() throws StatusCodeWithException {
        LongAdder progress = new LongAdder();
        // 批处理
        BatchConsumer<RsaPsiRecord> consumer = new BatchConsumer<>(batchSize, 5_000, records -> {
            try {
                List<RsaPsiRecord> fruits = matchOneBatch(records);

                saveFruits(fruits);

                // 更新进度
                fruitCount.add(fruits.size());
                progress.add(records.size());
                phaseProgress.updateCompletedWorkload(progress.longValue());
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                job.finishJobOnException(e);
            }
        });

        // 从数据源逐条读取数据集并编码
        job.getMyself().tableDataResourceReader.readRows((index, row) -> {

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
        for (RsaPsiRecord record : fruits) {
            writerForOnlyIds.write(
                    buildCsvDataLine(csvHeaderOnlyIds, record.row)
            );

            writerForWithAdditionalColumns.write(
                    buildCsvDataLine(csvHeaderWithAdditionalColumns, record.row)
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

    /**
     * 拼接用于输出到 csv 的主键相关字段列表
     */
    public String buildCsvDataLine(LinkedHashSet<String> csvHeader, LinkedHashMap<String, Object> row) {
        List<String> values = csvHeader.stream()
                .map(x -> {
                    Object value = row.get(x);
                    return value == null ? "" : value.toString();
                })
                .collect(Collectors.toList());

        return StringUtil.joinByComma(values) + System.lineSeparator();
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
}
