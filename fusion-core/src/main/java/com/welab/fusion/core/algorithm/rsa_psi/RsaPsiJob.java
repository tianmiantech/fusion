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
package com.welab.fusion.core.algorithm.rsa_psi;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.algorithm.base.PsiAlgorithm;

/**
 * @author zane.luo
 * @date 2023/12/20
 */
public class RsaPsiJob extends AbstractPsiJob {
    private RsaPsiJobMember myself, partner;
    private RsaPsiJobFunctions jobFunctions;

    public RsaPsiJob(String jobId, RsaPsiJobMember myself, RsaPsiJobMember partner, RsaPsiJobFunctions jobFunctions) {
        super(PsiAlgorithm.rsa_psi, jobId, myself, partner, jobFunctions);
        this.myself = myself;
        this.partner = partner;
        this.jobFunctions = jobFunctions;
    }

    @Override
    public RsaPsiJobFunctions getJobFunctions() {
        return jobFunctions;
    }

    @Override
    public RsaPsiJobMember getMyself() {
        return myself;
    }

    @Override
    public RsaPsiJobMember getPartner() {
        return partner;
    }
}
