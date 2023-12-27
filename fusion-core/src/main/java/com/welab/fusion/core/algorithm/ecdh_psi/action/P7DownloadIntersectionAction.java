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
package com.welab.fusion.core.algorithm.ecdh_psi.action;

import com.welab.fusion.core.Job.JobRole;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;

/**
 * @author zane.luo
 * @date 2023/12/26
 */
public class P7DownloadIntersectionAction extends AbstractJobPhaseAction<RsaPsiJob> {
    public P7DownloadIntersectionAction(RsaPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        job.getTempJobData().resultFileOnlyKey = super.downloadFileFromPartner("正在从合作方下载交集...");
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadIntersection;
    }

    @Override
    public long getTotalWorkload() {
        return 0;
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == JobRole.leader;
    }
}
