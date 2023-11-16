/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.fusion.service.dto.entity;

import com.alibaba.fastjson.JSONObject;
import com.welab.fusion.service.database.entity.DataSourceDbModel;
import com.welab.wefe.common.ModelMapper;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tony.wang
 */
public class DataSourceOutputModel extends AbstractOutputModel {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceOutputModel.class);
    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据库类型，枚举(hive、impala、mysql)
     */
    private DatabaseType databaseType;

    /**
     * 数据库IP地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    private JSONObject connectorConfig;


    public static DataSourceOutputModel of(DataSourceDbModel model) throws StatusCodeWithException {
        DataSourceOutputModel output = ModelMapper.map(model, DataSourceOutputModel.class);
        // 根据实体类标注进行脱敏
        output.connectorConfig = model.getJdbcDataSourceClient().getParams().toOutputJson();
        return output;
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
