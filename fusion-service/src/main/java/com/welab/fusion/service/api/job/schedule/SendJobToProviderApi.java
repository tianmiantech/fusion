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
package com.welab.fusion.service.api.job.schedule;

import com.welab.fusion.service.dto.entity.MemberInputModel;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
@Api(path = "job/send_to_provider", name = "发送任务到协作方")
public class SendJobToProviderApi extends AbstractApi<SendJobToProviderApi.Input, SendJobToProviderApi.Output> {
    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<SendJobToProviderApi.Output> handle(Input input) throws Exception {
        jobService.sendJobToProvider(input);
        return success();
    }

    public static class Input extends MemberInputModel {
        @Check(name = "任务ID", require = true)
        public String jobId;
    }

    public static class Output {
    }
}
