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

import com.welab.fusion.core.Job.FusionJobRole;
import com.welab.fusion.core.Job.FusionResult;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.Downloader;
import com.welab.fusion.service.api.download.base.FileType;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class SaveFusionResultFunction implements com.welab.fusion.core.function.SaveFusionResultFunction {
    private static final JobService jobService = Launcher.getBean(JobService.class);

    @Override
    public void save(String jobId, FusionJobRole myRole, FusionResult result, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws IOException, StatusCodeWithException {
        JobDbModel job = jobService.findById(jobId);

        switch (myRole) {
            // 如果我是过滤器提供方，则需要从协作方下载求交结果。
            case psi_bool_filter_provider:
                FusionResult fusionResult = downloadFusionResult(job, totalSizeConsumer, downloadSizeConsumer);
                saveFusionResult(job, fusionResult);
                break;

            // 如果我是数据提供方，则我已经有了求交结果，直接保存即可。
            case table_data_resource_provider:
                saveFusionResult(job, result);
                break;
            default:
                return;
        }
    }

    /**
     * 从协作方下载融合结果
     */
    private FusionResult downloadFusionResult(JobDbModel job, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws IOException, StatusCodeWithException {
        Downloader downloader = new Downloader(
                job.getId(),
                job.getPartnerMemberId(),
                FileType.FusionResult,
                JObject.create("jobId", job.getId()),
                fileInfo -> {
                    return FileSystem.FusionResult.getFile(job.getId()).toPath();
                }
        );

        downloader.setTotalSizeConsumer(totalSizeConsumer);
        downloader.setCompletedSizeConsumer(downloadSizeConsumer);

        File file = downloader.download();
        return null;
    }

    /**
     * 保存融合结果到本地
     */
    private void saveFusionResult(JobDbModel job, FusionResult result) {

    }
}
