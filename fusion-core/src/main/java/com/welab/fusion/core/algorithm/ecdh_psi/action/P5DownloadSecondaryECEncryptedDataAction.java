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

import com.welab.fusion.core.Job.JobRole;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
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
public class P5DownloadSecondaryECEncryptedDataAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    public P5DownloadSecondaryECEncryptedDataAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        return job.getMyJobRole() == JobRole.follower;
    }

    @Override
    protected void doAction() throws Exception {
        phaseProgress.setMessage("正在从合作方下载二次加密后的数据...");
        File file = downloadFileFromPartner();

        phaseProgress.setMessage("正在解压 zip 文件(" + InformationSize.fromByte(file.length()) + ")...");
        // file 解压至 dir
        Path dir = FileSystem.getTempDir()
                .resolve(job.getJobId())
                .resolve(
                        job.getPartner().memberId.replace(":", "_")
                                + "-SecondaryECEncryptedData-"
                                + FileUtil.getFileNameWithoutSuffix(file.getName())
                );
        SuperDecompressor.decompression(file, dir.toAbsolutePath().toString(), false);

        PsiECEncryptedData psiECEncryptedData = PsiECEncryptedData.of(dir);

        // 保存到上下文供后续使用
        job.getPartner().psiECEncryptedData = psiECEncryptedData;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadSecondaryECEncryptedData;
    }

    @Override
    public long getTotalWorkload() {
        return 100;
    }
}
