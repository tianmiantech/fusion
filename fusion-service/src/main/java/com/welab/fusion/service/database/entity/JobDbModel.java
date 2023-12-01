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
package com.welab.fusion.service.database.entity;

import com.welab.fusion.core.Job.JobStatus;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Entity(name = "job")
public class JobDbModel extends AbstractDbModel {
    @Check(name = "创建任务的成员ID")
    private String creatorMemberId;
    @Check(name = "合作方ID")
    private String partnerMemberId;
    @Check(name = "合作方名称")
    private String partnerMemberName;

    @Enumerated(EnumType.STRING)
    private JobMemberRole role;

    private String remark;

    @Check(name = "求交结果")
    private String resultFile;
    @Check(name = "交集数量")
    private long fusionCount;
    @Check(name = "任务开始时间")
    private Date startTime;
    @Check(name = "任务结束时间")
    private Date endTime;
    @Check(name = "任务耗时")
    private long costTime;
    @Check(name = "任务状态")
    private JobStatus status;
    private String message;

    // region getter/setter

    public String getCreatorMemberId() {
        return creatorMemberId;
    }

    public void setCreatorMemberId(String creatorMemberId) {
        this.creatorMemberId = creatorMemberId;
    }

    public String getPartnerMemberId() {
        return partnerMemberId;
    }

    public void setPartnerMemberId(String partnerMemberId) {
        this.partnerMemberId = partnerMemberId;
    }

    public String getPartnerMemberName() {
        return partnerMemberName;
    }

    public void setPartnerMemberName(String partnerMemberName) {
        this.partnerMemberName = partnerMemberName;
    }

    public JobMemberRole getRole() {
        return role;
    }

    public void setRole(JobMemberRole role) {
        this.role = role;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public long getFusionCount() {
        return fusionCount;
    }

    public void setFusionCount(long fusionCount) {
        this.fusionCount = fusionCount;
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

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // endregion
}
