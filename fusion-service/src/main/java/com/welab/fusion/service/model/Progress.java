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
package com.welab.fusion.service.model;

import com.welab.wefe.common.Convert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.UUID;

/**
 * 关于某活动的进度
 *
 * @author zane.luo
 * @date 2023/11/10
 */
public class Progress {
    private String sessionId;
    /**
     * 处理中的实体 Id
     */
    private String modelId;
    /**
     * 总工作量
     */
    private long totalWorkload;
    /**
     * 已完成工作量
     */
    private long completedWorkload;
    /**
     * 开始时间
     */
    private Date startTime = new Date();
    /**
     * 结束时间
     */
    private Date endTime;
    private String message;
    private ProgressStatus status;

    public static Progress of(String modelId, long totalWorkload) {
        Progress progress = new Progress();
        progress.modelId = modelId;
        progress.sessionId = UUID.randomUUID().toString().replace("-", "");
        progress.totalWorkload = totalWorkload;
        progress.status = ProgressStatus.doing;
        return progress;
    }

    public void success() {
        finish(ProgressStatus.completed, "success.");
    }

    public void failed(Exception e) {
        finish(ProgressStatus.failed, e.getMessage());
    }

    public void finish(ProgressStatus status, String message) {
        if (!status.isFinished()) {
            throw new RuntimeException("意料之外的状态：" + status);
        }
        this.endTime = new Date();
        this.message = message;
        this.status = status;

        if (status == ProgressStatus.completed) {
            this.completedWorkload = this.totalWorkload;
        }
    }

    /**
     * 更新已完成工作量
     */
    public void updateCompletedWorkload(long completedWorkload) {
        this.completedWorkload = completedWorkload;
    }

    /**
     * 耗时
     */
    public long getCostTime() {
        if (endTime == null) {
            return System.currentTimeMillis()
                    - startTime.getTime();
        }

        return endTime.getTime() - startTime.getTime();
    }

    /**
     * 进度，百分比，0 ~ 100。
     */
    public int getPercent() {
        if (completedWorkload <= 0 || totalWorkload <= 0) {
            return 0;
        }

        return Convert.toInt(completedWorkload * 100L / totalWorkload);
    }


    /**
     * 预计剩余时间
     */
    public long getEstimatedRemainingTime() {
        if (getPercent() <= 0 || getCostTime() <= 0) {
            return -1;
        }


        return BigDecimal.valueOf(totalWorkload - completedWorkload)
                .divide(BigDecimal.valueOf(completedWorkload), 5, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(getCostTime())).longValue();
    }

    public void updateTotalWorkload(Long totalWorkload) {
        this.totalWorkload = totalWorkload;
    }

    // region getter/setter

    public String getSessionId() {
        return sessionId;
    }

    public String getModelId() {
        return modelId;
    }

    public long getTotalWorkload() {
        return totalWorkload;
    }

    public long getCompletedWorkload() {
        return completedWorkload;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getMessage() {
        return message;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // endregion
}
