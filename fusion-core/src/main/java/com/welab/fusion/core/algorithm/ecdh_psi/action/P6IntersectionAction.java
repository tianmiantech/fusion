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
package com.welab.fusion.core.algorithm.ecdh_psi.action;

import com.welab.fusion.core.Job.FusionResult;
import com.welab.fusion.core.Job.JobRole;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.EllipticCurve;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import org.bouncycastle.math.ec.ECPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P6IntersectionAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    private static final int batchSize = 100_000;
    LongAdder progress = new LongAdder();

    public P6IntersectionAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        FusionResult result = job.getJobResult();
        LongAdder fruitCount = new LongAdder();
        File resultFile = FileSystem.FusionResult.getFileOnlyKeyColumns(job.getJobId());

        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;
        String headLine = hashConfig.getIdHeadersForCsv() + System.lineSeparator();

        Files.write(
                resultFile.toPath(),
                headLine.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE
        );

        int partnerPartitionIndex = 0;
        while (true) {
            Set<ECPoint> partnerPartition = readPartnerPartition(partnerPartitionIndex);
            partnerPartitionIndex++;
            if (partnerPartition.isEmpty()) {
                break;
            }

            LinkedList<ECPoint> points = matchOnePartition(partnerPartition);

            List<String> lines = points.stream()
                    .map(x -> x.toString())
                    .collect(Collectors.toList());

            // 向 resultFile 中写入交集数据
            Files.write(
                    resultFile.toPath(),
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
            );

            fruitCount.add(lines.size());

            if (partnerPartition.size() < batchSize) {
                break;
            }
        }

        result.fusionCount = fruitCount.longValue();
    }

    /**
     * 从我方读取全量数据与协作方二次加密数据的分片进行碰撞
     */
    private LinkedList<ECPoint> matchOnePartition(Set<ECPoint> partnerPartition) throws IOException {
        LinkedList<ECPoint> fruit = new LinkedList<>();

        File myFile = job.getMyself().secondaryECEncryptedDataFile;
        try (BufferedReader reader = FileUtil.buildBufferedReader(myFile)) {

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                progress.increment();
                phaseProgress.updateCompletedWorkload(progress.longValue());

                if (StringUtil.isBlank(line)) {
                    continue;
                }

                ECPoint point = EllipticCurve.INSTANCE.base64ToECPoint(line);
                if (partnerPartition.contains(point)) {
                    fruit.add(point);
                }
            }
        }

        return fruit;
    }

    /**
     * 从协作方二次加密数据中读取指定分片
     */
    private Set<ECPoint> readPartnerPartition(int partitionIndex) throws IOException {
        File file = job.getPartner().secondaryECEncryptedDataFile;
        List<String> lines = FileUtil.readPartitionLines(file, partitionIndex, batchSize, false);

        Set<ECPoint> result = new HashSet<>(batchSize);
        for (String line : lines) {
            ECPoint point = EllipticCurve.INSTANCE.base64ToECPoint(line);
            result.add(point);
        }

        return result;
    }


    @Override
    public JobPhase getPhase() {
        return JobPhase.Intersection;
    }

    @Override
    public long getTotalWorkload() {
        long count1 = job.getMyself().dataResourceInfo.dataCount;
        long count2 = job.getPartner().dataResourceInfo.dataCount;
        return count1 * count2;
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == JobRole.follower;
    }
}
