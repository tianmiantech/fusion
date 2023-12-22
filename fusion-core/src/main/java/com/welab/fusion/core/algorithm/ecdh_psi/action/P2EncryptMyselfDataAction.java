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

import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedDataCreator;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;

/**
 * @author zane.luo
 * @date 2023/12/19
 */
public class P2EncryptMyselfDataAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    public P2EncryptMyselfDataAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        AbstractTableDataSourceReader reader = job.getMyself().tableDataResourceReader;
        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;

        phaseProgress.setMessage("正在对我方数据进行加密...");
        try (PsiECEncryptedDataCreator creator = new PsiECEncryptedDataCreator(
                job.getJobId(),
                reader,
                hashConfig,
                phaseProgress)
        ) {
            PsiECEncryptedData data = creator.create();
            data.sink();

            // 保存对象至上下文对象，供后续阶段使用。
            job.getMyself().psiECEncryptedData = data;
        }
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.ECEncryptMyselfData;
    }

    @Override
    public long getTotalWorkload() {
        return job.getMyself().dataResourceInfo.dataCount;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }
}
