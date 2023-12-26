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

import com.welab.fusion.core.Job.JobRole;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.wefe.common.util.FileUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.math.ec.ECPoint;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/12/26
 */
public class P6AppendAdditionalResultColumnsAction extends AbstractJobPhaseAction<RsaPsiJob> {
    private static final int batchSize = 100_000;
    public P6AppendAdditionalResultColumnsAction(RsaPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        LinkedHashSet<String> headerOnlyIds = super.getResultFileCsvHeaderOnlyIds();
        LinkedHashSet<String> headerWithAdditionalColumns = super.getResultFileCsvHeaderWithAdditionalColumns();

        // 比较两个集合，找出需要添加的列。
        LinkedHashSet<String> needAppendColumns = new LinkedHashSet<>();
        for (String column : headerWithAdditionalColumns) {
            if (!headerOnlyIds.contains(column)) {
                needAppendColumns.add(column);
            }
        }

        if (needAppendColumns.isEmpty()) {
            phaseProgress.skipThisPhase();
            return;
        }


        BufferedWriter writer = FileUtil.buildBufferedWriter(job.getJobResult().resultFileWithAdditionalColumns, false);
int partitionIndex = 0;
        while (true) {
            List<String> partition = FileUtil.readPartition(
                    job.getJobResult().resultFileOnlyIds,
                    partitionIndex,
                    batchSize
            );
            if (partition.isEmpty()) {
                break;
            }

            LinkedList<ECPoint> points = appendOnePartition(partition);

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



        try () {
            job.getMyself().tableDataResourceReader.readRows(
                    (index, row) -> {
                        // 为每一行数据添加额外的结果列
                        List<String> values = needAppendColumns.stream()
                                .map(x -> {
                                    Object value = row.get(x);
                                    return value == null ? "" : value.toString();
                                }).collect(Collectors.toList());

                    }
            );
        }

    }

    private LinkedList<ECPoint> appendOnePartition(List<String> partition) {
        return null;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.AppendAdditionalResultColumns;
    }

    @Override
    public long getTotalWorkload() {
        return job.getMyself().dataResourceInfo.dataCount;
    }

    @Override
    protected boolean skipThisAction() {
        // leader 已经在求交环节添加了额外的结果列
        if (job.getMyJobRole() == JobRole.leader) {
            return true;
        }

        // 无需添加额外的结果列
        if (CollectionUtils.isEmpty(job.getMyself().dataResourceInfo.additionalResultColumns)) {
            return true;
        }

        return false;
    }
}
