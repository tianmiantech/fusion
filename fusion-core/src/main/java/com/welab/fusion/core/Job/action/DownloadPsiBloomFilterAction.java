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
import com.welab.fusion.core.function.DownloadPartnerPsiBloomFilterFunction;
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.file.decompression.SuperDecompressor;
import com.welab.wefe.common.util.FileUtil;

import java.io.File;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class DownloadPsiBloomFilterAction extends AbstractJobPhaseAction {
    public DownloadPsiBloomFilterAction(FusionJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == FusionJobRole.psi_bool_filter_provider;
    }

    @Override
    protected void doAction() throws Exception {
        DownloadPartnerPsiBloomFilterFunction function = job.getJobFunctions().downloadPartnerPsiBloomFilterFunction;

        // 从合作方下载过滤器
        File file = function.download(
                job.getJobId(),
                job.getPartner().memberId,
                size -> {
                    phaseProgress.updateTotalWorkload(size);
                },
                size -> {
                    phaseProgress.updateCompletedWorkload(size);
                });

        // file 解压至 dir
        Path dir = FileSystem.PsiBloomFilter.getPath(job.getPartner().memberId + "-" + FileUtil.getFileNameWithoutSuffix(file.getName()));
        SuperDecompressor.decompression(file, dir.toAbsolutePath().toString(), false);

        // 加载过滤器
        job.getPartner().psiBloomFilter = PsiBloomFilter.of(dir);
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadPsiBloomFilter;
    }

    @Override
    public long getTotalWorkload() {
        return 100;
    }
}
