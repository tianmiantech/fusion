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
package com.welab.fusion.core.algorithm.rsa_psi.action;

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.algorithm.rsa_psi.Role;
import com.welab.fusion.core.algorithm.rsa_psi.JobPhase;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.function.DownloadPartnerPsiBloomFilterFunction;
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.InformationSize;
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
        return job.getMyJobRole() == Role.psi_bool_filter_provider;
    }

    @Override
    protected void doAction() throws Exception {
        phaseProgress.setMessage("正在从合作方下载过滤器...");
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

        phaseProgress.setMessage("正在解压过滤器 zip 文件(" + InformationSize.fromByte(file.length()) + ")...");
        // file 解压至 dir
        Path dir = FileSystem.PsiBloomFilter.getPath(job.getPartner().memberId.replace(":", "_") + "-" + FileUtil.getFileNameWithoutSuffix(file.getName()));
        SuperDecompressor.decompression(file, dir.toAbsolutePath().toString(), false);


        PsiBloomFilter psiBloomFilter = PsiBloomFilter.of(dir);
        InformationSize size = InformationSize.fromByte(psiBloomFilter.getDataFile().length());

        LOG.info("正在加载过滤器，job_id：{}，psiBloomFilter:{}", job.getJobId(), psiBloomFilter.id);
        phaseProgress.setMessage("正在加载过滤器(" + size + ")...");
        // 将过滤器文件加载到内存是个很重的操作，所以是按需加载的，调用 getBloomFilter() 可触发加载。
        psiBloomFilter.getBloomFilter();
        LOG.info(
                "过滤器加载完毕({})，job_id：{}，psiBloomFilter:{}",
                size,
                job.getJobId(),
                psiBloomFilter.id
        );


        // 保存到 job 上下文供后续使用
        job.getPartner().psiBloomFilter = psiBloomFilter;
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