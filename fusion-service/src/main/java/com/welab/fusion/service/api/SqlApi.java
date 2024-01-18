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
package com.welab.fusion.service.api;


import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.Set;

/**
 * @author zane.luo
 * @date 2024/1/18
 */
@Api(path = "sql", name = "sql", allowAccessWithNothing = true)
public class SqlApi extends AbstractApi<SqlApi.Input, SqlApi.Output> {
    @Override
    protected ApiResult<SqlApi.Output> handle(SqlApi.Input input) throws Exception {
        return success(new Output());
    }

    public static class Input extends AbstractApiInput {
    }

    public static class Output {
        public Set<String> list;
    }
}
