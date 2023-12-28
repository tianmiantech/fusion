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
package com.welab.fusion.core.algorithm.rsa_psi.phase;

import com.welab.fusion.core.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;

import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P2CreatePsiBloomFilterAction extends AbstractJobPhaseAction<RsaPsiJob> {
    @Override
    protected void doAction() throws Exception {
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(job.getJobTempData().allOriginalData);
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

    public P2CreatePsiBloomFilterAction(RsaPsiJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        // 角色不是过滤器方，不生成。
        if (job.getMyJobRole() == JobRole.leader) {
            phaseProgress.setMessage("我方为数据集提供方，无需生成过滤器。");
            return true;
        }

        // 资源类型已经是过滤器，不生成。
        if (job.getMyself().dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {
            phaseProgress.setMessage("使用已有过滤器，无需生成。");
            return true;
        }

        return false;
    }

}
