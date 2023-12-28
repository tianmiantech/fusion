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
package com.welab.fusion.core.algorithm.base.phase_action;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.Job.PsiJobResult;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/12/28
 */
public class P6DownloadIntersectionAction<T extends AbstractPsiJob> extends AbstractJobPhaseAction<T> {
    private static final int batchSize = 100_000;
    private BufferedWriter writer;

    public P6DownloadIntersectionAction(T job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        PsiJobResult result = job.getJobResult();

        if (job.getMyJobRole() == JobRole.follower) {
            File file = super.downloadFileFromPartner("正在从合作方下载交集...");
            job.getJobTempData().resultFileOnlyKey = file;
            result.fusionCount = FileUtil.getFileLineCount(file);
        }

        createIntersectionOriginalData(result);
    }

    /**
     * 根据求交结果，将全量的明文减少到交集范围的明文。
     *
     * 由于明文数据不能传输，所以求交结果为 hash，这里要根据 hash 还原明文，顺便减少后续需要处理的数据量。
     */
    private void createIntersectionOriginalData(PsiJobResult result) throws Exception {
        phaseProgress.updateTotalWorkload(result.fusionCount);
        phaseProgress.setMessage("正在将交集密文还原为我方字段...");

        this.writer = initIntersectionOriginalData();

        int partitionIndex = 0;
        while (true) {
            List<String> partition = FileUtil.readPartitionLines(
                    job.getJobTempData().resultFileOnlyKey,
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
        try (BufferedReader reader = FileUtil.buildBufferedReader(job.getJobTempData().allOriginalData)) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                String key = line.split(",")[Constant.KEY_COLUMN_INDEX];
                if (set.contains(key)) {
                    writer.write(line + System.lineSeparator());
                }
            }
        }
    }

    /**
     * 初始化结果文件：交集部分的原始数据
     */
    private BufferedWriter initIntersectionOriginalData() throws IOException {
        if (job.getJobTempData().intersectionOriginalData != null) {
            throw new RuntimeException("intersectionOriginalData is not null");
        }

        File file = FileSystem.JobTemp.getIntersectionOriginalData(job.getJobId());
        job.getJobTempData().intersectionOriginalData = file;

        BufferedWriter writer = FileUtil.buildBufferedWriter(file, false);
        writer.write(
                headerToCsvLine(
                        getOriginalHeaderWithAdditionalColumns()
                )
        );

        return writer;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadIntersection;
    }

    @Override
    public long getTotalWorkload() {
        return 1;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }
}
