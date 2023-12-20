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

import java.nio.ByteBuffer;

/**
 * @author zane.luo
 * @date 2023/12/20
 */
public class EncryptPartnerDataAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    public EncryptPartnerDataAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {

    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.EncryptPartnerData;
    }

    @Override
    public long getTotalWorkload() {
        return job.getPartner().psiECEncryptedData.insertedElementCount;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }

}
