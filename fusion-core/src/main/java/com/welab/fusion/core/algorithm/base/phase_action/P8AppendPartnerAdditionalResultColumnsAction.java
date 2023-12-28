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
package com.welab.fusion.core.algorithm.base.phase_action;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.algorithm.JobPhase;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author zane.luo
 * @date 2023/12/26
 */
public class P8AppendPartnerAdditionalResultColumnsAction<T extends AbstractPsiJob> extends AbstractJobPhaseAction<T> {
    public P8AppendPartnerAdditionalResultColumnsAction(T job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        job.getTempJobData().resultFileWithPartnerAdditionalColumns = super.downloadFileFromPartner("正在从合作方下载附加结果字段..");
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.AppendPartnerAdditionalResultColumns;
    }

    @Override
    public long getTotalWorkload() {
        return 1;
    }

    @Override
    protected boolean skipThisAction() {
        return CollectionUtils.isNotEmpty(
                job.getPartner().dataResourceInfo.additionalResultColumns
        );
    }
}
