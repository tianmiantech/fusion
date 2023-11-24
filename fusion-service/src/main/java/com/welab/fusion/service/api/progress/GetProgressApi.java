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
package com.welab.fusion.service.api.progress;

import com.welab.fusion.core.progress.Progress;
import com.welab.fusion.service.model.ProgressManager;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author zane.luo
 * @date 2023/11/17
 */
@Api(path = "progress/get", name = "获取任务进度")
public class GetProgressApi extends AbstractApi<GetProgressApi.Input,GetProgressApi.Output> {
    @Override
    protected ApiResult<GetProgressApi.Output> handle(GetProgressApi.Input input) throws Exception {
        Progress progress = ProgressManager.get(input.sessionId);
        return success(Output.of(progress));
    }

    public static class Input extends AbstractApiInput {
        public String sessionId;
    }

    public static class Output {
        public Progress progress;

        public static Output of(Progress progress) {
            Output output = new Output();
            output.progress = progress;
            return output;
        }
    }
}
