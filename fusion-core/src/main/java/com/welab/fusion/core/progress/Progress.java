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

import cn.hutool.core.date.DateUtil;
import com.welab.wefe.common.Convert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
public class Progress {
    protected String modelId;
    protected String sessionId;
    /**
     * 总工作量
     */
    protected long totalWorkload = -1;
    /**
     * 已完成工作量
     */
    protected long completedWorkload = -1;
    /**
     * 速度，以秒为单位。
     */
    protected long speedInSecond;
    /**
     * 上一次更新进度的时间
     */
    private long lastUpdateCompletedWorkloadTime = System.currentTimeMillis();
    /**
     * 开始时间
     */
    protected Date startTime = new Date();
    /**
     * 结束时间
     */
    protected Date endTime;
    protected String message;
    /**
     * message 记录
     */
    private List<String> logs = new ArrayList<>();
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
     * 并更新速度
     */
    public synchronized void updateCompletedWorkload(long completedWorkload) {
        BigDecimal increment = BigDecimal.valueOf(completedWorkload - this.completedWorkload);
        BigDecimal cost = BigDecimal.valueOf(System.currentTimeMillis() - lastUpdateCompletedWorkloadTime);
        if (cost.intValue() > 0) {
            this.speedInSecond = increment.divide(cost, 5, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(1_000))
                    .longValue();
        } else {
            this.speedInSecond = 0;
        }

        this.lastUpdateCompletedWorkloadTime = System.currentTimeMillis();
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
        if (totalWorkload == 0) {
            return 100;
        }
        if (completedWorkload <= 0) {
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

        if (getPercent() == 100) {
            return 0;
        }

        return BigDecimal.valueOf(totalWorkload - completedWorkload)
                .divide(BigDecimal.valueOf(completedWorkload), 5, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(getCostTime())).longValue();
    }

    public void updateTotalWorkload(Long totalWorkload) {
        this.totalWorkload = totalWorkload;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageAndLog(String message) {
        this.message = message;

        synchronized (logs) {
            if (logs.size() > 100) {
                logs.remove(0);
            }
            logs.add("[" + DateUtil.now() + "] " + message);
        }
    }

    // region getter/setter

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getTotalWorkload() {
        return totalWorkload;
    }

    public void setTotalWorkload(long totalWorkload) {
        this.totalWorkload = totalWorkload;
    }

    public long getCompletedWorkload() {
        return completedWorkload;
    }

    public void setCompletedWorkload(long completedWorkload) {
        this.completedWorkload = completedWorkload;
    }

    public long getSpeedInSecond() {
        return speedInSecond;
    }

    public void setSpeedInSecond(long speedInSecond) {
        this.speedInSecond = speedInSecond;
    }

    public long getLastUpdateCompletedWorkloadTime() {
        return lastUpdateCompletedWorkloadTime;
    }

    public void setLastUpdateCompletedWorkloadTime(long lastUpdateCompletedWorkloadTime) {
        this.lastUpdateCompletedWorkloadTime = lastUpdateCompletedWorkloadTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    // endregion
}
