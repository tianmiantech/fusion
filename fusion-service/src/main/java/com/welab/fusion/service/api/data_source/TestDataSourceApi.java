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
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "data_source/test", name = "测试数据源的可用性")
public class TestDataSourceApi extends AbstractApi<TestDataSourceApi.Input, TestDataSourceApi.Output> {
    @Autowired
    private DataSourceService dataSourceService;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        try {
            dataSourceService.testDataSource(input);
            return success(Output.success());
        } catch (Exception e) {
            return success(Output.fail(e.getMessage()));
        }
    }

    public static class Input extends AddDataSourceApi.Input {
        @Check(name = "数据源Id", desc = "添加时不需要指定，修改时需要指定。")
        public String id;

    }

    public static class Output {
        @Check(name = "测试结果（成功/失败）")
        public boolean success;
        @Check(name = "测试结果消息")
        public String message;

        public static Output success() {
            Output output = new Output();
            output.success = true;
            output.message = "该数据库连接可用";
            return output;
        }

        public static Output fail(String message) {
            Output output = new Output();
            output.success = false;
            output.message = message;
            return output;
        }
    }
}
