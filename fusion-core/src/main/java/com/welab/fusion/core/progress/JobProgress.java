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
package com.welab.fusion.core.progress;

import com.alibaba.fastjson.JSONObject;
import com.welab.fusion.core.Job.base.JobStatus;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.wefe.common.util.JObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class JobProgress {
    public List<JobPhaseProgress> phases = new ArrayList<>();

    public void addPhaseProgress(JobPhaseProgress phaseProgress) {
        phases.add(phaseProgress);
    }

    /**
     * 返回最后一个阶段的 message
     */
    public String getMessage() {
        if (phases.isEmpty()) {
            return null;
        }
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
        if (currentPhaseProgress.getJobStatus().isFailed()) {
            return currentPhaseProgress.getJobStatus();
        }

        // 如果已经到最后一个阶段，且已成功，则整个任务成功。
        if (currentPhaseProgress.getJobPhase().isLastPhase() && currentPhaseProgress.getJobStatus().isSuccess()) {
            return JobStatus.success;
        }

        // 其它情况都判定为运行中
        return JobStatus.running;
    }

    /**
     * 获取当前进度的状态
     */
    public JobStatus getCurrentPhaseStatus() {
        if (phases.isEmpty()) {
            return JobStatus.wait_run;
        }

        return getCurrentPhaseProgress().getJobStatus();
    }

    /**
     * 获取当前阶段的进度
     */
    public JobPhaseProgress getCurrentPhaseProgress() {
        if (phases.isEmpty()) {
            return null;
        }
        return phases.get(phases.size() - 1);
    }

    /**
     * 获取当前阶段
     */
    public JobPhase getCurrentPhase() {
        if (phases.isEmpty()) {
            return null;
        }
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
        return phases.isEmpty();
    }

    public JSONObject toJson() {
        return JObject.create(this);
    }
}
