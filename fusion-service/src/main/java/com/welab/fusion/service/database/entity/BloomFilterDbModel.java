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
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.fusion.service.constans.BloomFilterAddMethod;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Entity(name = "bloom_filter")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class BloomFilterDbModel extends AbstractDbModel {
    /**
     * 资源名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 存储路径
     */
    private String storageDir;
    /**
     * 存储大小
     */
    private String storageSize;
    /**
     * 总数据量
     */
    private long totalDataCount;
    /**
     * 主键hash生成方法
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject hashConfigs;

    /**
     * 布隆过滤器添加方式
     */
    @Enumerated(EnumType.STRING)
    private BloomFilterAddMethod addMethod;
    /**
     * sql语句
     */
    private String sql;

    // region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public String getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(String storageSize) {
        this.storageSize = storageSize;
    }

    public long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(long totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public JSONObject getHashConfigs() {
        return hashConfigs;
    }

    public void setHashConfigs(JSONObject hashConfigs) {
        this.hashConfigs = hashConfigs;
    }

    public BloomFilterAddMethod getAddMethod() {
        return addMethod;
    }

    public void setAddMethod(BloomFilterAddMethod addMethod) {
        this.addMethod = addMethod;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    // endregion
}
