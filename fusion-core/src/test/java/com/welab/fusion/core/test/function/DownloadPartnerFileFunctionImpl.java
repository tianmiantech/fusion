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
package com.welab.fusion.core.test.function;

import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.function.DownloadPartnerFileFunction;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.io.FileSystem;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class DownloadPartnerFileFunctionImpl implements DownloadPartnerFileFunction {

    @Override
    public File download(JobPhase jobPhase, String jobId, String partnerId, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws Exception {
        switch (jobPhase) {
            case DownloadPsiBloomFilter:
                Path dir = FileSystem.PsiBloomFilter.getDir(partnerId);
                PsiBloomFilter psiBloomFilter = PsiBloomFilter.of(dir);

                return psiBloomFilter.zip();
            case DownloadPartnerECEncryptedData:
                return PsiECEncryptedData.of(jobId).zip();
            case DownloadSecondaryECEncryptedData:
                return FileSystem
                        .PsiSecondaryECEncryptedData
                        .getDataFile(jobId);
            case SaveResult:
                return FileSystem.FusionResult.getFile(jobId);
            default:
                return null;
        }
    }
}
