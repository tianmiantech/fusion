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

import com.welab.fusion.core.progress.ProgressStatus;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public enum JobStatus {
    /**
     * 待执行
     */
    wait_run,
    /**
     * 执行中
     */
    running,
    /**
     * 手动停止
     */
    stop_on_running,
    /**
     * 异常停止
     */
    error_on_running,
    /**
     * 成功
     */
    success;

    public boolean isFinished() {
        switch (this) {
            case success:
            case stop_on_running:
            case error_on_running:
                return true;
            default:
                return false;
        }
    }

    public boolean isRunning() {
        return this == running;
    }

    public boolean isFailed() {
        switch (this) {
            case stop_on_running:
            case error_on_running:
                return true;
            default:
                return false;
        }
    }

    public boolean isSuccess() {
        return this == success;
    }

    /**
     * 将任务状态转换为通用进度状态
     */
    public ProgressStatus toProgressStatus() {
        switch (this) {
            case wait_run:
            case running:
                return ProgressStatus.doing;
            case stop_on_running:
            case error_on_running:
                return ProgressStatus.failed;
            case success:
                return ProgressStatus.completed;
            default:
                throw new RuntimeException("意料之外的状态：" + this);
        }
    }
}
