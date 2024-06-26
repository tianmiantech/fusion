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
package com.welab.fusion.service.api.download;

import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.base.FileInfo;
import com.welab.fusion.service.service.JobMemberService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/11/30
 */
@Api(path = "download/get_file_info", name = "下载文件分片", allowAccessWithSign = true)
public class GetDownloadFileInfoApi extends AbstractApi<GetDownloadFileInfoApi.Input, FileInfo> {
    @Autowired
    private JobMemberService jobMemberService;

    @Override
    protected ApiResult<FileInfo> handle(GetDownloadFileInfoApi.Input input) throws Exception {
        File file = findFile(input);
        if (file == null) {
            StatusCode.PARAMETER_VALUE_INVALID
                    .throwException("未找到 " + input.jobPhase + " 阶段需要的文件，任务Id：" + input.jobId);
        }
        return success(FileInfo.of(file));
    }

    private File findFile(Input input) throws IOException {
        switch (input.jobPhase) {
            case DownloadPsiBloomFilter:
                String bloomFilterId = jobMemberService.findMyself(input.jobId).getBloomFilterId();
                Path dir = FileSystem.PsiBloomFilter.getDir(bloomFilterId);
                return PsiBloomFilter.of(dir).zip();
            case DownloadPartnerECEncryptedData:
                return PsiECEncryptedData.of(input.jobId).zip();
            case DownloadSecondaryECEncryptedData:
                return FileSystem.PsiSecondaryECEncryptedData.getDataFile(input.jobId);
            case DownloadIntersection:
                return FileSystem.JobTemp.getFileOnlyKey(input.jobId);
            case AppendPartnerAdditionalResultColumns:
                return FileSystem.JobTemp.getFileWithMyselfAdditionalColumns(input.jobId);
            default:
                return null;
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务阶段", require = true)
        public JobPhase jobPhase;
        @Check(name = "任务Id", require = true)
        public String jobId;

        public static Input of(JobPhase jobPhase, String jobId) {
            Input input = new Input();
            input.jobPhase = jobPhase;
            input.jobId = jobId;
            return input;
        }
    }


}
