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
package com.welab.fusion.core.Job.action;

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.Job.FusionJobRole;
import com.welab.fusion.core.Job.JobPhase;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class SaveResultAction extends AbstractJobPhaseAction {
    public SaveResultAction(FusionJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        if (job.getMyJobRole() == FusionJobRole.psi_bool_filter_provider) {
            phaseProgress.setMessage("正在从合作方下载求交结果...");
        }else{
            phaseProgress.setMessage("正在保存求交结果...");
        }

        // 储存结果
        job.getJobFunctions().saveFusionResultFunction.save(
                job.getJobId(),
                job.getMyJobRole(),
                job.getJobResult(),
                totalSize -> phaseProgress.updateTotalWorkload(totalSize),
                downloadSize -> phaseProgress.updateCompletedWorkload(downloadSize)
        );

        phaseProgress.setMessage("保存完毕");
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.SaveResult;
    }

    @Override
    public long getTotalWorkload() {
        return 0;
    }

    /**
     * 所有角色都需要下载结果
     */
    @Override
    protected boolean skipThisAction() {
        return false;
    }
}
