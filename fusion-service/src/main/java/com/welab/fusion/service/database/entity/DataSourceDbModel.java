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
import com.welab.fusion.service.database.entity.id_class.DataSourceDbModelId;
import com.welab.wefe.common.data.source.JdbcDataSourceClient;
import com.welab.wefe.common.data.source.SuperDataSourceClient;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Entity(name = "data_source")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class DataSourceDbModel extends AbstractDbModel {
    private String name;
    @Enumerated(EnumType.STRING)
    private DatabaseType databaseType;

    private String host;
    private Integer port;

    /**
     * 前端 form
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject connectorConfig;

    @JSONField(serialize = false)
    public JdbcDataSourceClient getJdbcDataSourceClient() throws StatusCodeWithException {
        return SuperDataSourceClient.create(databaseType.name(), connectorConfig);
    }

    /**
     * 由于用户未编辑密码时前端不会传递此字段，所以需要用数据库的数据补齐。
     */
    public void padLostParams(Map<String, Object> input) {
        for (Map.Entry<String, Object> item : getConnectorConfig().entrySet()) {
            // 补齐前端未传的参数（密码）
            if (!input.containsKey(item.getKey()) || input.get(item.getKey()) == null) {
                input.put(item.getKey(), item.getValue());
            }
        }
    }

    // region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public JSONObject getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(JSONObject connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    // endregion
}
