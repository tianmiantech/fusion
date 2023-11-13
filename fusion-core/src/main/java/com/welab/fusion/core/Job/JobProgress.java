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
package com.welab.fusion.core.Job;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class JobProgress {
    public List<JobPhaseProgress> jobPhaseProgressList = new ArrayList<>();

    /**
     * 初始化，填充初始阶段的进度。
     */
    public void init(String jobId, JobMember myself) {
        JobPhaseProgress phaseProgress = JobPhaseProgress.of(
                jobId,
                JobPhase.firstPhase(),
                myself.dataResourceInfo.dataCount
        );
        jobPhaseProgressList.add(phaseProgress);
    }

    public void addPhaseProgress(JobPhaseProgress phaseProgress) {
        jobPhaseProgressList.add(phaseProgress);
    }

    /**
     * 返回最后一个阶段的 message
     */
    public String getMessage() {
        return getCurrentPhaseProgress().getMessage();
    }

    /**
     * 获取当前阶段的状态
     */
    public JobStatus getCurrentPhaseStatus() {
        return getCurrentPhaseProgress().getStatus();
    }

    /**
     * 获取当前阶段的进度
     */
    public JobPhaseProgress getCurrentPhaseProgress() {
        return jobPhaseProgressList.get(jobPhaseProgressList.size() - 1);
    }

    /**
     * 获取当前阶段
     */
    public JobPhase getCurrentPhase() {
        return getCurrentPhaseProgress().getJobPhase();
    }

    /**
     * 结束任务
     */
    public void finish(JobStatus status, String message) {
        getCurrentPhaseProgress().finish(status, message);
    }
}
