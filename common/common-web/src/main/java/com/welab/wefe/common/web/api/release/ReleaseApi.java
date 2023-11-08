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
package com.welab.wefe.common.web.api.release;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author zane.luo
 * @date 2023/3/10
 */
@Api(path = "release", name = "获取版本信息")
public class ReleaseApi extends AbstractApi<ReleaseApi.Input, ReleaseApi.Output> {
    public static final String VERSION = "3.7.1";
    public static final String TIME = "2023-06-06 14:30:14";
    @Override
    protected ApiResult<ReleaseApi.Output> handle(ReleaseApi.Input input) throws Exception {
        return success(new Output());
    }

    public static class Output {
        public String version = VERSION;
        public String time = TIME;
    }

    public static class Input extends AbstractApiInput {
    }
}
