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

import com.alibaba.fastjson.JSONObject;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.entity.MemberDbModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class JobMemberOutputModel extends AbstractOutputModel {
    @Check(name = "成员名称")
    private String memberName;
    @Check(name = "成员Id")
    private String memberId;
    @Check(name = "公钥")
    private String publicKey;
    @Check(name = "服务端地址")
    private String baseUrl;
    @Enumerated(EnumType.STRING)
    private JobMemberRole role;
    @Enumerated(EnumType.STRING)
    private DataResourceType dataResourceType;
    private long totalDataCount;

    @Check(name = "主键hash生成方法")
    private JSONObject hashConfig;

    @Check(name = "过滤器Id")
    private String bloomFilterId;

    @Check(name = "数据源信息")
    private JSONObject tableDataResourceInfo;

    public static JobMemberOutputModel of(MemberDbModel member, JobMemberDbModel jobMember) {
        if (jobMember == null) {
            return null;
        }

        JobMemberOutputModel output = jobMember.mapTo(JobMemberOutputModel.class);
        if (member != null) {
            output.memberName = member.getName();
            output.publicKey = member.getPublicKey();
            output.baseUrl = member.getBaseUrl();
        }
        return output;
    }

    // region getter/setter

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JobMemberRole getRole() {
        return role;
    }

    public void setRole(JobMemberRole role) {
        this.role = role;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public JSONObject getHashConfig() {
        return hashConfig;
    }

    public void setHashConfig(JSONObject hashConfig) {
        this.hashConfig = hashConfig;
    }

    public String getBloomFilterId() {
        return bloomFilterId;
    }

    public void setBloomFilterId(String bloomFilterId) {
        this.bloomFilterId = bloomFilterId;
    }

    public JSONObject getTableDataResourceInfo() {
        return tableDataResourceInfo;
    }

    public void setTableDataResourceInfo(JSONObject tableDataResourceInfo) {
        this.tableDataResourceInfo = tableDataResourceInfo;
    }

    // endregion
}
