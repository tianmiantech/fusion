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
package com.welab.fusion.core.Job.action;

import cn.hutool.core.codec.Base64;
import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.Job.FusionJobRole;
import com.welab.fusion.core.Job.FusionResult;
import com.welab.fusion.core.Job.JobPhase;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.psi.PsiRecord;
import com.welab.fusion.core.psi.PsiUtils;
import com.welab.wefe.common.BatchConsumer;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class IntersectionAction extends AbstractJobPhaseAction {
    private static final int batchSize = 50000;
    /**
     * E
     */
    private BigInteger publicExponent;
    /**
     * N
     */
    private BigInteger publicModulus;

    public IntersectionAction(FusionJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {

        publicModulus = job.getPartner().psiBloomFilter.rsaPsiParam.publicModulus;
        publicExponent = job.getPartner().psiBloomFilter.rsaPsiParam.publicExponent;

        FusionResult result = job.getJobResult();
        LongAdder progress = new LongAdder();
        LongAdder fruitCount = new LongAdder();
        File resultFile = FileSystem.FusionResult.getFile(job.getJobId());

        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;


        Files.write(
                resultFile.toPath(),
                hashConfig.getIdHeadersForCsv().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE
        );


        // 批处理
        BatchConsumer<PsiRecord> consumer = new BatchConsumer<>(batchSize, 5_000, records -> {
            try {
                // 将数据发送到过滤器方加密
                List<String> encryptedList = job.getJobFunctions().encryptPsiRecordsFunction.encrypt(
                        job.getJobId(),
                        job.getPartner().memberId,
                        records.stream().map(x -> x.encodedKey).collect(Collectors.toList())
                );

                // 碰撞，并获取交集。
                List<PsiRecord> fruit = PsiUtils.match(
                        job.getPartner().psiBloomFilter,
                        records,
                        encryptedList,
                        publicModulus
                );

                List<String> lines = fruit.stream()
                        .map(x -> hashConfig.getIdValuesForCsv(x.row))
                        .collect(Collectors.toList());

                // 向 resultFile 中写入交集数据
                Files.write(
                        resultFile.toPath(),
                        lines,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.APPEND
                );

                // 更新进度
                fruitCount.add(fruit.size());
                progress.add(records.size());
                phaseProgress.updateCompletedWorkload(progress.longValue());
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                job.finishJobOnException(e);
            }
        });

        // 从数据源逐条读取数据集并编码
        job.getMyself().tableDataResourceReader.readRows((index, row) -> {

            PsiRecord record = new PsiRecord();
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

        // 包装结果
        result.finish(resultFile, fruitCount.longValue());
    }


    @Override
    public JobPhase getPhase() {
        return JobPhase.Intersection;
    }

    @Override
    public long getTotalWorkload() {
        return job.getMyself().tableDataResourceReader.getTotalDataRowCount();
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == FusionJobRole.psi_bool_filter_provider;
    }
}
