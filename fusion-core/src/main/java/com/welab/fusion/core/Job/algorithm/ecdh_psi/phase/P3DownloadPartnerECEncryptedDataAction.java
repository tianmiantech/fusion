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
package com.welab.fusion.core.Job.algorithm.ecdh_psi.phase;

import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.wefe.common.InformationSize;
import com.welab.wefe.common.file.decompression.SuperDecompressor;

import java.io.File;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P3DownloadPartnerECEncryptedDataAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    public P3DownloadPartnerECEncryptedDataAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }

    @Override
    protected void doAction() throws Exception {
        File file = downloadFileFromPartner("正在下载合作方加密后的数据...");

        phaseProgress.setMessage("正在解压 zip 文件(" + InformationSize.fromByte(file.length()) + ")...");
        // file 解压至 dir
        Path dir = file.getParentFile().toPath();
        SuperDecompressor.decompression(file, dir.toAbsolutePath().toString(), false);

        // 保存到上下文供后续使用
        job.getPartner().psiECEncryptedData = PsiECEncryptedData.of(dir);
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.DownloadPartnerECEncryptedData;
    }

    @Override
    public long getTotalWorkload() {
        return 100;
    }
}
