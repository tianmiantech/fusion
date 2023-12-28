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
package com.welab.fusion.core.Job.algorithm.ecdh_psi.phase;

import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.algorithm.base.phase_action.AbstractIntersectionAction;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve.EllipticCurve;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import org.bouncycastle.math.ec.ECPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P6IntersectionAction extends AbstractIntersectionAction<EcdhPsiJob> {
    private static final int batchSize = 100_000;
    LongAdder progress = new LongAdder();

    public P6IntersectionAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {

        // 求交
        File intersection = intersection();

        // 生成供合作方使用的交集文件
        createIntersectionFile(intersection);

        intersection.delete();
        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;
    }


    /**
     * @return 交集文件，其内容为我方原始数据的索引。
     */
    private File intersection() throws Exception {
        long count1 = job.getMyself().dataResourceInfo.dataCount;
        long count2 = job.getPartner().dataResourceInfo.dataCount;
        phaseProgress.updateTotalWorkload(count1 * count2);

        // 这里使用密文进行求交，交集结果储存数据对应明文数据的索引。
        File file = FileSystem.JobTemp
                .getDir(job.getJobId())
                .resolve("intersection-index.data")
                .toFile();

        BufferedWriter writer = FileUtil.buildBufferedWriter(file, false);
        LongAdder fruitCount = new LongAdder();

        int partitionIndex = 0;
        while (true) {
            Set<ECPoint> partnerPartition = readPartnerPartition(partitionIndex);
            partitionIndex++;
            if (partnerPartition.isEmpty()) {
                break;
            }

            // 得到的交集信息是我方数据的索引
            LinkedList<String> indexList = matchOnePartition(partnerPartition);
            fruitCount.add(indexList.size());

            // 交集写入文件
            for (String line : indexList) {
                writer.write(line + System.lineSeparator());
            }

            if (partnerPartition.size() < batchSize) {
                break;
            }
        }
        writer.close();

        job.getJobResult().fusionCount = fruitCount.longValue();

        return file;
    }

    /**
     * 从我方读取全量数据与协作方二次加密数据的分片进行碰撞
     */
    private LinkedList<String> matchOnePartition(Set<ECPoint> partnerPartition) throws Exception {
        LinkedList<String> fruits = new LinkedList<>();

        File myFile = job.getMyself().secondaryECEncryptedDataFile;
        try (CsvTableDataSourceReader reader = new CsvTableDataSourceReader(myFile)) {

            reader.readRows(
                    (index, row) -> {
                        String key = row.get(Constant.KEY_COLUMN_NAME).toString();
                        ECPoint point = EllipticCurve.INSTANCE.base64ToECPoint(key);
                        if (partnerPartition.contains(point)) {
                            fruits.add(
                                    row.get(Constant.INDEX_COLUMN_NAME).toString()
                            );
                        }

                        progress.increment();
                        phaseProgress.updateCompletedWorkload(progress.longValue());
                    }
            );

        }

        return fruits;
    }

    /**
     * 从协作方二次加密数据中读取指定分片
     */
    private Set<ECPoint> readPartnerPartition(int partitionIndex) throws IOException {
        File file = job.getPartner().secondaryECEncryptedDataFile;
        List<String> lines = FileUtil.readPartitionLines(
                file,
                partitionIndex,
                batchSize,
                false
        );

        Set<ECPoint> result = new HashSet<>(batchSize);
        for (String line : lines) {
            ECPoint point = EllipticCurve.INSTANCE.base64ToECPoint(line);
            result.add(point);
        }

        return result;
    }

    /**
     * 生成供合作方使用的交集文件（在安全性上可暴露、可传输）
     */
    private void createIntersectionFile(File indexListFile) throws Exception {
        int partitionIndex = 0;
        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;

        try (BufferedWriter writer = super.initResultFileOnlyKey()) {
            while (true) {
                List<String> partition = FileUtil.readPartitionLines(
                        indexListFile,
                        partitionIndex,
                        batchSize,
                        false
                );
                partitionIndex++;
                if (partition.isEmpty()) {
                    break;
                }

                writeOnePartition(partition, hashConfig, writer);


                if (partition.size() < batchSize) {
                    break;
                }
            }
        }
    }

    private void writeOnePartition(List<String> partition, HashConfig hashConfig, BufferedWriter writer) throws IOException, StatusCodeWithException {
        Set<String> set = new HashSet<>(partition);
        try (CsvTableDataSourceReader reader = new CsvTableDataSourceReader(job.getJobTempData().allOriginalData)) {
            reader.readRows(
                    (index, row) -> {
                        String lineIndex = row.get(Constant.INDEX_COLUMN_NAME).toString();
                        if (set.contains(lineIndex)) {
                            try {
                                String key = hashConfig.hash(row);
                                writer.write(key + System.lineSeparator());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.Intersection;
    }

    @Override
    public long getTotalWorkload() {
        return 1;
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == JobRole.follower;
    }
}
