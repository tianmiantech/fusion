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
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.CloseableUtils;
import com.welab.wefe.common.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;

/**
 * @author zane.luo
 * @date 2023/12/27
 */
public abstract class AbstractInitJobAction<T extends AbstractPsiJob> extends AbstractJobPhaseAction<T> {
    public AbstractInitJobAction(T job) {
        super(job);
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.InitJob;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }

    @Override
    public long getTotalWorkload() {
        return 1;
    }

    /**
     * 将数据从数据源转运到工作区以 csv 格式存储
     *
     * 1. 转运时仅保留任务需要用到的字段，减少数据体积。
     * 2. 由于任务过程中可能多次全量读取原始数据，改为读取转运后的 csv 性能更佳。
     * 3. 为避免任务过程中数据源中的数据发生变化，导致结果不一致，所以在任务开始时，将数据源转运到工作区。
     * 4. ecdh-psi 在求交时得到的交集为密文，使用 index 与原始明文进行映射，所以需要将原始数据保存在工作区，确保映射关系稳定。
     */
    protected void loadOriginalDataToJobWorkspace() throws IOException, StatusCodeWithException {
        if (job.getMyself().dataResourceInfo.dataResourceType != DataResourceType.TableDataSource) {
            return;
        }

        phaseProgress.setMessageAndLog("正在从数据源加载数据至工作区...");
        phaseProgress.updateTotalWorkload(job.getMyself().dataResourceInfo.dataCount);
        phaseProgress.updateCompletedWorkload(0);

        LinkedHashSet<String> header = getOriginalHeaderWithAdditionalColumns();
        File file = FileSystem.JobTemp.getAllOriginalData(job.getJobId());
        try (BufferedWriter writer = FileUtil.buildBufferedWriter(file, false)) {
            writer.write(headerToCsvLine(header));

            job.getMyself().tableDataResourceReader.readRows(
                    (index, row) -> {
                        try {

                            String key = job.getMyself().dataResourceInfo.hashConfig.hash(row);
                            row.put(Constant.INDEX_COLUMN_NAME, index);
                            row.put(Constant.KEY_COLUMN_NAME, key);

                            writer.write(
                                    rowToCsvLine(header, row)
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        phaseProgress.updateCompletedWorkload(index + 1);
                    }
            );

            CloseableUtils.closeQuietly(job.getMyself().tableDataResourceReader);
        }

        job.getJobTempData().allOriginalData = file;
    }
}
