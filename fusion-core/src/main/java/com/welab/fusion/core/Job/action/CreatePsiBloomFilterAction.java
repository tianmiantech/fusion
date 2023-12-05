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
import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;

import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class CreatePsiBloomFilterAction extends AbstractJobPhaseAction {
    @Override
    protected void doAction() throws Exception {
        AbstractTableDataSourceReader reader = job.getMyself().tableDataResourceReader;
        HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;

        phaseProgress.setMessage("正在生成过滤器...");
        // 生成过滤器
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator(
                UUID.randomUUID().toString().replace("-", ""),
                reader,
                hashConfig,
                phaseProgress)
        ) {
            PsiBloomFilter psiBloomFilter = creator.create();

            // 保存对象至上下文对象，供后续阶段使用。
            job.getMyself().psiBloomFilter = psiBloomFilter;

            // 保存过滤器
            phaseProgress.setMessage("过滤器生成完毕，正在保存...");
            job.getJobFunctions().saveMyPsiBloomFilterFunction.save(job.getJobId(), psiBloomFilter);
        }
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.CreatePsiBloomFilter;
    }

    @Override
    public long getTotalWorkload() {
        return job.getMyself().dataResourceInfo.dataCount;
    }

    public CreatePsiBloomFilterAction(FusionJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        // 仅在我方提供的是数据集，但需要以过滤器身份执行时，才需要创建过滤器。
        boolean needCreate = job.getMyJobRole() == FusionJobRole.psi_bool_filter_provider
                && job.getMyself().dataResourceInfo.dataResourceType == DataResourceType.TableDataSource;

        return !needCreate;
    }

}
