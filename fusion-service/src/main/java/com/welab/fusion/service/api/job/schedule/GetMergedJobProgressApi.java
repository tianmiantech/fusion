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

import com.welab.fusion.core.Job.base.JobStatus;
import com.welab.fusion.core.progress.JobProgress;
import com.welab.fusion.service.database.entity.MemberDbModel;
import com.welab.fusion.service.service.GatewayService;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/12/4
 */
@Api(
        path = "job/get_merged_job_progress",
        name = "获取多方合并的任务进度",
        allowAccessWithSign = true,
        logSamplingInterval = 1_000 * 30
)
public class GetMergedJobProgressApi extends AbstractApi<GetMergedJobProgressApi.Input, GetMergedJobProgressApi.Output> {
    @Autowired
    private JobService jobService;
    @Autowired
    private GatewayService gatewayService;

    @Override
    protected ApiResult<Output> handle(GetMergedJobProgressApi.Input input) throws Exception {

        JobProgress myselfProgress = jobService.getMyJobProgress(input.jobId);

        MemberDbModel partner = jobService.findPartner(input.jobId);
        if (partner == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("此任务尚未推送给合作方");
        }
        FusionNodeInfo partnerNodeInfo = partner.toFusionNodeInfo();
        JobProgress partnerProgress = gatewayService.callOtherFusionNode(
                partnerNodeInfo,
                GetMyJobProgressApi.class,
                GetMyJobProgressApi.Input.of(input.jobId)
        );

        if (myselfProgress.getJobStatus() == JobStatus.running && partnerProgress.getJobStatus().isFinished()) {
            jobService.finishOnPartnerFinished(input.jobId);
        }

        return success(Output.of(myselfProgress, partnerProgress));
    }

    public static class Input extends AbstractApiInput {
        public String jobId;
    }

    public static class Output {
        @Check(name = "我方进度")
        public JobProgress myself;
        @Check(name = "合作方进度")
        public JobProgress partner;

        public static Output of(JobProgress myself, JobProgress partner) {
            Output output = new Output();
            output.myself = myself;
            output.partner = partner;
            return output;
        }
    }
}
