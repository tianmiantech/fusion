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

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.service.config.fastjson.BlockForPartnerField;
import com.welab.fusion.service.service.DataSourceService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.data.source.JdbcDataSourceParams;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.common.web.TempSm2Cache;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author zane.luo
 */
@Api(
        path = "data_source/save",
        name = "新增或更新数据源",
        desc = "以 host+port 为唯一标识，如果已存在，则更新。"
)
public class SaveDataSourceApi extends AbstractApi<SaveDataSourceApi.Input, SaveDataSourceApi.Output> {

    @Autowired
    private DataSourceService dataSourceService;

    @Override
    protected ApiResult<SaveDataSourceApi.Output> handle(Input input) throws Exception {
        String id = dataSourceService.save(input);
        return success(Output.of(id));
    }

    public static class Input extends AbstractApiInput {
        public DatabaseType databaseType;

        @Secret(maskStrategy = MaskStrategy.MAP_WITH_PASSWORD)
        @BlockForPartnerField
        public Map<String, Object> dataSourceParams;

        @JSONField(serialize = false)
        public String getHost() {
            Object host = dataSourceParams.get("host");
            if (host == null) {
                return null;
            }
            return (String) host;
        }

        @JSONField(serialize = false)
        public Integer getPort() {
            Object port = dataSourceParams.get("port");
            if (port == null) {
                return null;
            }
            return Convert.toInt(port);
        }

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (dataSourceParams != null) {
                dataSourceParams.put("databaseType", databaseType.name());
                // 参数解密
                dataSourceParams = TempSm2Cache.decryptMap(dataSourceParams, JdbcDataSourceParams.class);
            }
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
