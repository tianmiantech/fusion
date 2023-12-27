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

import com.welab.fusion.core.Job.FusionResult;
import com.welab.fusion.core.Job.JobRole;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.wefe.common.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/12/26
 */
public class P5DownloadIntersectionAction extends AbstractJobPhaseAction<RsaPsiJob> {
    private static final int batchSize = 100_000;
    private BufferedWriter writer;

    public P5DownloadIntersectionAction(RsaPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        FusionResult result = job.getJobResult();

        if (job.getMyJobRole() == JobRole.follower) {
            result.resultFileOnlyKey = super.downloadFileFromPartner("正在从合作方下载交集...");
            result.fusionCount = FileUtil.getFileLineCount(result.resultFileOnlyKey);
        }

        phaseProgress.updateTotalWorkload(result.fusionCount);
        phaseProgress.setMessage("正在将交集密文还原为我方字段...");

        this.writer = super.initIntersectionOriginalData();

        int partitionIndex = 0;
        while (true) {
            List<String> partition = FileUtil.readPartitionLines(
                    job.getJobResult().resultFileOnlyKey,
                    partitionIndex,
                    batchSize,
                    false
            );
            partitionIndex++;

            if (partition.isEmpty()) {
                break;
            }

            expandOnePartition(partition);

            if (partition.size() < batchSize) {
                break;
            }
        }

        this.writer.close();
    }

    private void expandOnePartition(List<String> partition) throws Exception {
        HashSet<String> set = new HashSet<>(partition);
        try (BufferedReader reader = FileUtil.buildBufferedReader(job.getMyself().allOriginalData)) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                String key = line.split(",")[0];
                if (set.contains(key)) {
                    writer.write(line + System.lineSeparator());
                }
            }
        }
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadIntersection;
    }

    @Override
    public long getTotalWorkload() {
        return 0;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }
}
