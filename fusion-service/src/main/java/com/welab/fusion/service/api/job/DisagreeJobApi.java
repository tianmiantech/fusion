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
package com.welab.fusion.service.api.job;

import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
@Api(path = "job/disagree", name = "协作方拒绝任务")
public class DisagreeJobApi extends AbstractApi<DisagreeJobApi.Input, DisagreeJobApi.Output> {
    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<Output> handle(DisagreeJobApi.Input input) throws Exception {
        jobService.disagree(input);
        return success();
    }

    public static class Output {
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "拒绝的原因")
        public String reason;
    }
}
