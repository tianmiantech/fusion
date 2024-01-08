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
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.algorithm.base.PsiAlgorithm;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.util.FileUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;

/**
 * @author zane.luo
 * @date 2023/12/26
 */
public class P7AppendMyselfAdditionalResultColumnsAction<T extends AbstractPsiJob> extends AbstractJobPhaseAction<T> {
    public P7AppendMyselfAdditionalResultColumnsAction(T job) {
        super(job);
    }

    /**
     * 在 Key 的基础上，拼接额外的结果列，生成新的结果文件，这个文件会被协作方下载。
     *
     * 由于在前置阶段已经生成了 intersectionOriginalData，
     * 所以这里只需根据新的 header 生成新的结果文件即可。
     */
    @Override
    protected void doAction() throws Exception {
        phaseProgress.setMessageAndLog("正在拼接我方附加结果列...");

        LinkedHashSet<String> header = getHeader();
        try (BufferedWriter writer = initFile()) {

            try (CsvTableDataSourceReader reader = new CsvTableDataSourceReader(job.getJobTempData().intersectionOriginalData)) {
                reader.readRows((index, row) -> {
                    try {
                        writer.write(
                                super.rowToCsvLine(header, row)
                        );
                    } catch (IOException e) {
                        LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                    }

                    phaseProgress.updateCompletedWorkload(index);
                });
            }
        }
    }


    /**
     * 获取结果文件的 csv header（包含附加结果列）
     */
    private LinkedHashSet<String> getHeader() {
        LinkedHashSet<String> header = new LinkedHashSet<>();
        header.add(Constant.KEY_COLUMN_NAME);

        // 输出附加字段
        LinkedHashSet<String> additionalResultColumns = job.getMyself().dataResourceInfo.additionalResultColumns;
        if (additionalResultColumns != null) {
            header.addAll(additionalResultColumns);
        }

        return header;
    }

    /**
     * 初始化结果文件：包含我方附加列
     */
    private BufferedWriter initFile() throws Exception {
        if (job.getJobTempData().resultFileWithMyselfAdditionalColumns != null) {
            throw new RuntimeException("resultFileWithMyselfAdditionalColumns is not null");
        }

        // 初始化结果文件
        File file = FileSystem.JobTemp.getFileWithMyselfAdditionalColumns(job.getJobId());
        job.getJobTempData().resultFileWithMyselfAdditionalColumns = file;

        BufferedWriter writer = FileUtil.buildBufferedWriter(file, false);
        writer.write(
                headerToCsvLine(
                        getHeader()
                )
        );

        return writer;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.AppendMyselfAdditionalResultColumns;
    }

    @Override
    public long getTotalWorkload() {
        return job.getJobResult().fusionCount;
    }

    @Override
    protected boolean skipThisAction() {
        if (job.getAlgorithm() == PsiAlgorithm.rsa_psi) {
            if (job.getMyJobRole() == JobRole.leader && job.getMyself().dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {
                return true;
            }
        }

        return CollectionUtils.isEmpty(
                job.getMyself().dataResourceInfo.additionalResultColumns
        );
    }

    @Override
    public void close() throws IOException {

    }
}
