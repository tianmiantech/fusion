/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.fusion.service.database.entity;

import com.welab.fusion.service.database.repository.base.BaseRepository;
import com.welab.fusion.service.database.repository.base.RepositoryManager;
import com.welab.wefe.common.ModelMapper;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 数据库实体的抽象父类
 *
 * @author Zane
 */
@MappedSuperclass
public abstract class AbstractDbModel implements Serializable {

    /**
     * 全局唯一标识
     */
    @Id
    @Column(name = "id", updatable = false)
    private String id = generateId();
    /**
     * 创建时间
     */
    private Date createdTime = new Date();
    /**
     * 更新时间
     */
    private Date updatedTime;


    public void save() {
        BaseRepository repository = RepositoryManager.get(this.getClass());
        repository.save(this);
    }

    public void setUpdatedTimeNow() {
        this.updatedTime = new Date();
    }

    /**
     * 转换为其它实体
     */
    public <T> T mapTo(Class<T> clazz) {
        return ModelMapper.map(this, clazz);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    // endregion
}
