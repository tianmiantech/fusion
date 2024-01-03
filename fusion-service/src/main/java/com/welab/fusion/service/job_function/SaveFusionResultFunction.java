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

import com.welab.fusion.core.Job.PsiJobResult;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.web.Launcher;

import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class SaveFusionResultFunction implements com.welab.fusion.core.algorithm.rsa_psi.function.SaveFusionResultFunction {
    private static final JobService jobService = Launcher.getBean(JobService.class);

    @Override
    public void save(String jobId, JobRole myRole, PsiJobResult result, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws Exception {
        JobDbModel job = jobService.findById(jobId);

        saveFusionResult(job, result);
    }

    /**
     * 保存求交结果到本地
     */
    private void saveFusionResult(JobDbModel job, PsiJobResult result) {
        job.setFusionCount(result.fusionCount);
        job.setUpdatedTimeNow();

        job.save();
    }
}
