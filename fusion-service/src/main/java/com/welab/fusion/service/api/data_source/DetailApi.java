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

package com.welab.fusion.service.api.data_source;

import com.welab.fusion.service.database.entity.DataSourceDbModel;
import com.welab.fusion.service.database.repository.DataSourceRepository;
import com.welab.fusion.service.dto.entity.DataSourceOutputModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "data_source/detail", name = "获取数据源详情")
public class DetailApi extends AbstractApi<DetailApi.Input, DataSourceOutputModel> {

    @Autowired
    private DataSourceRepository DatasourceRepository;

    @Override
    protected ApiResult<DataSourceOutputModel> handle(Input input) throws StatusCodeWithException {

        DataSourceDbModel model = DatasourceRepository.findById(input.id).orElse(null);

        if (model == null) {
            return success();
        }

        DataSourceOutputModel output = DataSourceOutputModel.of(model);

        return success(output);

    }

    public static class Input extends AbstractApiInput {
        public String id;
    }
}
