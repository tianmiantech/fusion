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

package com.welab.fusion.service.api.data_source;

import com.welab.fusion.service.dto.entity.DataSourceOutputModel;
import com.welab.fusion.service.service.DataSourceService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "data_source/list", name = "列举全部数据源")
public class ListApi extends AbstractApi<ListApi.Input, ListApi.Output> {

    @Autowired
    DataSourceService dataSourceService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        List<DataSourceOutputModel> list = dataSourceService.list();
        return success(Output.of(list));
    }

    public static class Input extends AbstractApiInput {

    }

    public static class Output {
        public List<DataSourceOutputModel> list;

        public static Output of(List<DataSourceOutputModel> list) {
            Output output = new Output();
            output.list = list;
            return output;
        }

    }
}
