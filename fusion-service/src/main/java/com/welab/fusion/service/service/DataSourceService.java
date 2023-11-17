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
package com.welab.fusion.service.service;

import com.welab.fusion.service.api.data_source.AddDataSourceApi;
import com.welab.fusion.service.api.data_source.TestDataSourceApi;
import com.welab.fusion.service.api.data_source.UpdateApi;
import com.welab.fusion.service.database.entity.DataSourceDbModel;
import com.welab.fusion.service.database.repository.DataSourceRepository;
import com.welab.fusion.service.dto.entity.DataSourceOutputModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.source.DataSourceParams;
import com.welab.wefe.common.data.source.JdbcDataSourceClient;
import com.welab.wefe.common.data.source.SuperDataSourceClient;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Service
public class DataSourceService extends AbstractService {
    @Autowired
    private DataSourceRepository dataSourceRepository;

    public DataSourceDbModel findById(String id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    public String add(AddDataSourceApi.Input input) throws StatusCodeWithException {
        JdbcDataSourceClient dataSourceClient = SuperDataSourceClient.create(input.databaseType.name(), input.dataSourceParams);
        dataSourceClient.test();

        if (dataSourceRepository.countByName(dataSourceClient.getParams().name) > 0) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "此数据源名称已存在，请换一个数据源名称");
        }

        checkUniqueness(null, dataSourceClient.getParams());

        DataSourceDbModel model = new DataSourceDbModel();
        model.setName(dataSourceClient.getParams().name);
        model.setDatabaseType(input.databaseType);


        model.setDatabaseType(input.databaseType);
        model.setHost(dataSourceClient.getParams().getHost());
        model.setPort(dataSourceClient.getParams().getPort());
        model.setConnectorConfig(dataSourceClient.getParams().toJson());

        dataSourceRepository.save(model);

        return model.getId();
    }

    /**
     * 检查填写的 host:port 是否已添加过，禁止重复添加。
     */
    public void checkUniqueness(DataSourceDbModel oldModel, DataSourceParams params) throws StatusCodeWithException {

        List<DataSourceDbModel> list = dataSourceRepository.findByHostAndPort(
                params.host,
                params.port
        );

        // 查重无记录
        if (list.isEmpty()) {
            return;
        }

        // 查重查到自己
        if (oldModel != null && list.size() == 1 && list.get(0).getId().equals(oldModel.getId())) {
            return;
        }

        throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "此数据源已存在");
    }

    public void delete(String id) {
        dataSourceRepository.deleteById(id);
    }

    public List<DataSourceOutputModel> list() {
        List<DataSourceDbModel> all = dataSourceRepository.findAll();
        return ModelMapper.maps(all, DataSourceOutputModel.class);
    }

    public void testDataSource(TestDataSourceApi.Input input) throws StatusCodeWithException {
        JdbcDataSourceClient client = StringUtil.isNotEmpty(input.id)
                // 前端传了 id，从数据库取出数据源配置
                ? dataSourceRepository.findById(input.id).orElse(null).getJdbcDataSourceClient()
                // 前端没传 id，直接用前端传的数据源配置
                : SuperDataSourceClient.create(input.databaseType.name(), input.dataSourceParams);

        client.test();
    }

    public void update(UpdateApi.Input input) throws StatusCodeWithException {
        JdbcDataSourceClient client = SuperDataSourceClient.create(
                input.databaseType.name(),
                input.dataSourceParams
        );
        client.test();

        DataSourceDbModel model = dataSourceRepository.findById(input.id).orElse(null);
        if (model == null) {
            return;
        }

        DataSourceParams params = client.getParams();

        checkUniqueness(model, params);
        if ((!(model.getName().equals(params.getName())) && dataSourceRepository.countByName(params.getName()) > 0)) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "此数据源名称已存在，请换一个数据源名称");
        }

        model.setName(params.getName());
        model.setHost(params.getHost());
        model.setPort(params.getPort());
        model.setConnectorConfig(params.toJson());

        dataSourceRepository.save(model);
    }
}
