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

package com.welab.fusion.service.api.service;

import com.welab.fusion.service.service.InitService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(
        path = "service/is_initialized",
        allowAccessWithNothing = true,
        name = "服务是否已初始化",
        desc = "服务在初始化之前无法访问任何功能模块"
)
public class IsInitializedApi extends AbstractNoneInputApi<IsInitializedApi.Output> {

    @Autowired
    private InitService initService;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        return success(Output.of(initService.isInitialized()));
    }

    public static class Output {
        public boolean initialized;

        public static Output of(boolean initialized) {
            Output output = new Output();
            output.initialized = initialized;
            return output;
        }
    }
}
