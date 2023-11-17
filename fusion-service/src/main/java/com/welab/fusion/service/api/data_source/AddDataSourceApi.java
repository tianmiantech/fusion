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

import com.welab.fusion.service.service.DataSourceService;
import com.welab.wefe.common.data.source.JdbcDataSourceParams;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.TempSm2Cache;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author Johnny.lin
 */
@Api(path = "data_source/add", name = "添加数据源")
public class AddDataSourceApi extends AbstractApi<AddDataSourceApi.Input, AddDataSourceApi.Output> {
    @Autowired
    DataSourceService dataSourceService;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        String id = dataSourceService.add(input);
        return success(Output.of(id));
    }

    public static class Input extends AbstractApiInput {
        @Check(messageOnEmpty = "数据库类型不能为空")
        public DatabaseType databaseType;

        @Check(require = true)
        public Map<String, Object> dataSourceParams;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            dataSourceParams.put("databaseType", databaseType.name());
            // 参数解密
            dataSourceParams = TempSm2Cache.decryptMap(dataSourceParams, JdbcDataSourceParams.class);
        }
    }

    public static class Output {
        public String id;

        public static Output of(String id) {
            Output output = new Output();
            output.id = id;
            return output;
        }
    }
}

