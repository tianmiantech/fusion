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
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;

/**
 * @author zane.luo
 * @date 2023/12/28
 */
public abstract class AbstractIntersectionAction <T extends AbstractPsiJob> extends AbstractJobPhaseAction<T>{
    public AbstractIntersectionAction(T job) {
        super(job);
    }

    /**
     * 初始化结果文件：仅包含主键列
     */
    protected BufferedWriter initResultFileOnlyKey() throws Exception {
        if (job.getJobTempData().resultFileOnlyKey != null) {
            throw new RuntimeException("resultFileOnlyKey is not null");
        }

        File file = FileSystem.JobTemp.getFileOnlyKey(job.getJobId());
        job.getJobTempData().resultFileOnlyKey = file;

        return FileUtil.buildBufferedWriter(file, false);
    }
}
