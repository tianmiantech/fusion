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
package com.welab.fusion.service.dto.entity;

import com.welab.fusion.core.Job.JobStatus;
import com.welab.fusion.core.algorithm.base.PsiAlgorithm;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.Date;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class JobOutputModel extends AbstractOutputModel {
    @Check(name = "算法")
    private PsiAlgorithm algorithm;
    @Check(name = "创建任务的成员ID")
    private String creatorMemberId;
    @Check(name = "我方角色")
    private JobMemberRole role;
    @Check(name = "任务备注")
    private String remark;
    @Check(name = "交集数量")
    private Long fusionCount;
    @Check(name = "任务开始时间")
    private Date startTime;
    @Check(name = "任务结束时间")
    private Date endTime;
    @Check(name = "任务耗时")
    private Long costTime;
    @Check(name = "任务状态")
    private JobStatus status;
    @Check(name = "任务状态对应的消息")
    private String message;
    @Check(name = "我方资源信息")
    private JobMemberOutputModel myself;
    @Check(name = "合作方资源信息")
    private JobMemberOutputModel partner;

    public static JobOutputModel of(JobDbModel job, JobMemberOutputModel myself, JobMemberOutputModel partner) {
        JobOutputModel output = job.mapTo(JobOutputModel.class);
        output.myself = myself;
        output.partner = partner;
        return output;
    }

    // region getter/setter


    public PsiAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(PsiAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public String getCreatorMemberId() {
        return creatorMemberId;
    }

    public void setCreatorMemberId(String creatorMemberId) {
        this.creatorMemberId = creatorMemberId;
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

    public Long getFusionCount() {
        return fusionCount;
    }

    public void setFusionCount(Long fusionCount) {
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

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
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

    public JobMemberOutputModel getMyself() {
        return myself;
    }

    public void setMyself(JobMemberOutputModel myself) {
        this.myself = myself;
    }

    public JobMemberOutputModel getPartner() {
        return partner;
    }

    public void setPartner(JobMemberOutputModel partner) {
        this.partner = partner;
    }


    // endregion
}
