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

import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.function.DownloadPartnerFileFunction;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.Downloader;
import com.welab.fusion.service.api.download.base.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class DownloadPartnerFileFunctionImpl implements DownloadPartnerFileFunction {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());


    @Override
    public File download(JobPhase jobPhase, String jobId, String partnerId, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws Exception {
        Downloader downloader = new Downloader(
                jobPhase,
                jobId,
                partnerId,
                // 指定存储路径
                fileInfo -> {
                    return buildFilePath(jobPhase, jobId, partnerId, fileInfo);
                }
        );

        downloader.setTotalSizeConsumer(totalSizeConsumer);
        downloader.setCompletedSizeConsumer(downloadSizeConsumer);

        return downloader.download();
    }

    /**
     * 构建文件储存路径
     */
    private static File buildFilePath(JobPhase jobPhase, String jobId, String partnerId, FileInfo fileInfo) {
        switch (jobPhase) {
            case SaveResult:
                return FileSystem.FusionResult.getFile(jobId);

            default:
                return FileSystem.JobTemp.getFile(jobId, jobPhase, partnerId, fileInfo.filename);
        }
    }


}
