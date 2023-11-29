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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Entity(name = "job_member")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class JobMemberDbModel extends AbstractDbModel {
    private String jobId;
    @Check(name = "合作方Id")
    private String memberId;

    @Enumerated(EnumType.STRING)
    private JobMemberRole role;
    @Enumerated(EnumType.STRING)
    private DataResourceType dataResourceType;
    private long totalDataCount;
    /**
     * 主键hash生成方法
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject hashConfig;

    @Check(name = "过滤器Id")
    private String bloomFilterId;

    @Check(name = "数据源信息")
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject tableDataResourceInfo;

    @JSONField(serialize = false)
    public HashConfig getHashConfigModel() {
        if (hashConfig == null) {
            return null;
        }
        return hashConfig.toJavaObject(HashConfig.class);
    }

    // region getter/setter

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String partnerId) {
        this.memberId = partnerId;
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
