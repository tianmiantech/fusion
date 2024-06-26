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

import com.welab.fusion.core.progress.JobProgress;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/12/4
 */
@Api(
        path = "job/get_my_job_progress",
        name = "获取我放的任务进度",
        allowAccessWithSign = true,
        logSamplingInterval = 1_000 * 30
)
public class GetMyJobProgressApi extends AbstractApi<GetMyJobProgressApi.Input, JobProgress> {
    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<JobProgress> handle(GetMyJobProgressApi.Input input) throws Exception {
        JobProgress jobProgress = jobService.getMyJobProgress(input.jobId);
        return success(jobProgress);
    }

    public static class Input extends AbstractApiInput {
        public String jobId;

        public static Input of(String jobId) {
            Input input = new Input();
            input.jobId = jobId;
            return input;
        }
    }
}
