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
package com.welab.fusion.core.Job.algorithm.ecdh_psi.phase;

import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.EcdhPsiJob;

import java.io.File;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P5DownloadSecondaryECEncryptedDataAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    public P5DownloadSecondaryECEncryptedDataAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == JobRole.follower;
    }

    @Override
    protected void doAction() throws Exception {
        File file = downloadFileFromPartner("正在从合作方下载二次加密后的数据...");

        // 保存到上下文供后续使用
        job.getMyself().secondaryECEncryptedDataFile = file;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadSecondaryECEncryptedData;
    }

    @Override
    public long getTotalWorkload() {
        return 100;
    }
}
