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
import com.welab.fusion.core.Job.JobPhase;
import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.function.GlobalFunctions;
import com.welab.wefe.common.file.decompression.SuperDecompressor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        return false;
    }

    @Override
    protected void doAction() throws Exception {
        // 从合作方下载过滤器
        File file = GlobalFunctions.downloadPsiBloomFilterFunction.download(
                job.getPartner().memberId,
                job.getJobId(),
                size -> {
                    phaseProgress.updateTotalWorkload(size);
                },
                size -> {
                    phaseProgress.updateCompletedWorkload(size);
                });

        // file 解压至 dir
        Path dir = Paths.get("");
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
