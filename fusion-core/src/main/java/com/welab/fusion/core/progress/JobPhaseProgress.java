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

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.base.JobStatus;

/**
 * 关于某阶段的任务进度
 *
 * @author zane.luo
 * @date 2023/11/10
 */
public class JobPhaseProgress extends Progress {
    private JobPhase jobPhase;
    private JobStatus jobStatus;
    /**
     * 是否跳过
     */
    private boolean skipThisPhase;

    public static JobPhaseProgress of(String jobId, JobPhase jobPhase, long totalWorkload) {
        JobPhaseProgress progress = new JobPhaseProgress();
        progress.sessionId = jobId;
        progress.jobPhase = jobPhase;
        progress.totalWorkload = totalWorkload;
        progress.jobStatus = JobStatus.running;
        progress.setStatus(ProgressStatus.doing);
        return progress;
    }

    public void finish(JobStatus status, String message) {

        if (!status.isFinished()) {
            throw new RuntimeException("意料之外的状态：" + status);
        }
        super.finish(status.toProgressStatus(), message);
        this.jobStatus = status;

        if (status == JobStatus.success) {
            this.completedWorkload = this.totalWorkload;
        }
    }

    @JSONField(serialize = false)
    public void skipThisPhase() {
        skipThisPhase("我方跳过此阶段");
    }

    @JSONField(serialize = false)
    public void skipThisPhase(String message) {
        if (skipThisPhase) {
            return;
        }
        finish(JobStatus.success, message);
        skipThisPhase = true;
    }

    // region getter/setter

    public JobPhase getJobPhase() {
        return jobPhase;
    }

    public void setJobPhase(JobPhase jobPhase) {
        this.jobPhase = jobPhase;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public boolean isSkipThisPhase() {
        return skipThisPhase;
    }

    public void setSkipThisPhase(boolean skipThisPhase) {
        this.skipThisPhase = skipThisPhase;
    }

    // endregion
}
