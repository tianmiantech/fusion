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
     * 获取整个任务的状态
     */
    public JobStatus getJobStatus() {
        if (isEmpty()) {
            return JobStatus.wait_run;
        }

        JobPhaseProgress currentPhaseProgress = getCurrentPhaseProgress();
        // 如果已经失败，整个任务失败
        if (currentPhaseProgress.getStatus().isFailed()) {
            return currentPhaseProgress.getStatus();
        }

        // 如果已经到最后一个阶段，且已成功，则整个任务成功。
        if (currentPhaseProgress.getJobPhase().isLastPhase() && currentPhaseProgress.getStatus().isSuccess()) {
            return JobStatus.success;
        }

        // 其它情况都判定为运行中
        return JobStatus.running;
    }

    /**
     * 获取当前进度的状态
     */
    public JobStatus getCurrentPhaseStatus() {
        if (jobPhaseProgressList.isEmpty()) {
            return JobStatus.wait_run;
        }

        return getCurrentPhaseProgress().getStatus();
    }

    /**
     * 获取当前阶段的进度
     */
    public JobPhaseProgress getCurrentPhaseProgress() {
        if (jobPhaseProgressList.isEmpty()) {
            return null;
        }
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

    /**
     * 打印进度信息到控制台
     */
    public void print() {
        System.out.println(getCurrentPhase().name() + "(" + getCurrentPhaseStatus() + "):" + getMessage());
    }

    public boolean isEmpty() {
        return jobPhaseProgressList.isEmpty();
    }
}
