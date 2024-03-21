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
package com.welab.fusion.service.job_function;

import com.welab.fusion.core.progress.JobProgress;
import com.welab.fusion.service.api.job.schedule.GetMyJobProgressApi;
import com.welab.fusion.service.service.GatewayService;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.FusionNodeInfo;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class GetPartnerProgressFunction implements com.welab.fusion.core.algorithm.rsa_psi.function.GetPartnerProgressFunction {
    private static final GatewayService gatewayService = Launcher.getBean(GatewayService.class);
    private static final JobService jobService = Launcher.getBean(JobService.class);

    @Override
    public JobProgress get(String jobId) throws Exception {
        FusionNodeInfo partner = jobService.findPartner(jobId).toFusionNodeInfo();
        JobProgress jobProgress = gatewayService.callOtherFusionNode(
                partner,
                GetMyJobProgressApi.class,
                GetMyJobProgressApi.Input.of(jobId)
        );
        return jobProgress;
    }
}
