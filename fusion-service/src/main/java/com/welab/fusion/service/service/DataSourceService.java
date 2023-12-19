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

import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.service.api.bloom_filter.AddBloomFilterApi;
import com.welab.fusion.service.api.data_source.SaveDataSourceApi;
import com.welab.fusion.service.api.data_source.TestDataSourceApi;
import com.welab.fusion.service.constans.AddMethod;
import com.welab.fusion.service.database.entity.DataSourceDbModel;
import com.welab.fusion.service.database.repository.DataSourceRepository;
import com.welab.fusion.service.dto.JobConfigInput;
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
import org.springframework.scheduling.annotation.Async;
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

    /**
     * 检查填写的 host:port 是否已添加过，禁止重复添加。
     */
    public void checkUniqueness(DataSourceDbModel oldModel, DataSourceParams params) throws StatusCodeWithException {

        DataSourceDbModel ont = dataSourceRepository.findByHostAndPort(
                params.host,
                params.port
        );

        // 查重无记录
        if (ont == null) {
            return;
        }

        // 查重查到自己
        if (oldModel != null && ont.getId().equals(oldModel.getId())) {
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

    public String testDataSource(TestDataSourceApi.Input input) {
        JdbcDataSourceClient client = StringUtil.isNotEmpty(input.id)
                // 前端传了 id，从数据库取出数据源配置
                ? dataSourceRepository.findById(input.id).orElse(null).getJdbcDataSourceClient()
                // 前端没传 id，直接用前端传的数据源配置
                : SuperDataSourceClient.create(input.databaseType.name(), input.dataSourceParams);

        return client.test();
    }

    /**
     * 有则修改，无则添加。
     */
    public String save(SaveDataSourceApi.Input input) throws StatusCodeWithException {
        DataSourceDbModel model = dataSourceRepository.findByHostAndPort(
                input.getHost(),
                input.getPort()
        );

        if (model != null) {
            model.padLostParams(input.dataSourceParams);
        }

        JdbcDataSourceClient dataSourceClient = SuperDataSourceClient.create(input.databaseType.name(), input.dataSourceParams);
        dataSourceClient.test();

        if (model == null) {
            model = new DataSourceDbModel();
        }

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
     * 由添加过滤器触发的创建数据源
     * 由于这里创建数据源是个顺带的操作，所以仅作尝试，失败时不抛出异常。
     */
    @Async
    public void trySave(AddBloomFilterApi.Input input) {
        if (input.isRequestFromPartner()) {
            return;
        }

        // 不是数据库类型的数据源，不创建。
        if (input.addMethod != AddMethod.Database) {
            return;
        }

        try {
            save(input);
        } catch (StatusCodeWithException e) {
            LOG.error(e.getClass().getSimpleName() + " 自动保存数据源失败：" + e.getMessage(), e);
        }
    }

    /**
     * 由创建/启动触发的创建数据源
     * 由于这里创建数据源是个顺带的操作，所以仅作尝试，失败时不抛出异常。
     */
    public void trySave(JobConfigInput input) {
        if (input.isRequestFromPartner()) {
            return;
        }
        
        // 不是数据集类型的数据源，不创建。
        if (input.dataResource.dataResourceType != DataResourceType.TableDataSource) {
            return;
        }

        // 不是数据库类型的数据源，不创建。
        if (input.dataResource.tableDataResourceInfo.addMethod != AddMethod.Database) {
            return;
        }

        try {
            save(input.dataResource.tableDataResourceInfo);
        } catch (StatusCodeWithException e) {
            LOG.error(e.getClass().getSimpleName() + " 自动保存数据源失败：" + e.getMessage(), e);
        }
    }
}
