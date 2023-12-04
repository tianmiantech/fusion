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
import com.welab.fusion.core.Job.JobStatus;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.Downloader;
import com.welab.fusion.service.api.download.base.FileType;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;

import java.io.File;
import java.util.function.Consumer;

import static com.welab.fusion.core.Job.FusionJobRole.psi_bool_filter_provider;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class SaveFusionResultFunction implements com.welab.fusion.core.function.SaveFusionResultFunction {
    private static final JobService jobService = Launcher.getBean(JobService.class);

    @Override
    public void save(String jobId, FusionJobRole myRole, FusionResult result, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws Exception {
        JobDbModel job = jobService.findById(jobId);

        // 如果我是过滤器提供方，则需要从协作方下载求交结果。
        if (myRole == psi_bool_filter_provider) {
            downloadFusionResult(job, result, totalSizeConsumer, downloadSizeConsumer);
        }

        saveFusionResult(job, result);
    }

    /**
     * 从协作方下载求交结果
     */
    private FusionResult downloadFusionResult(JobDbModel job, FusionResult result, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws Exception {
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

        result.resultFile = file;
        try (CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file)) {
            result.fusionCount = reader.getTotalDataRowCount();
        }

        return result;
    }

    /**
     * 保存求交结果到本地
     */
    private void saveFusionResult(JobDbModel job, FusionResult result) {
        job.setStartTime(result.startTime);
        job.setEndTime(result.endTime);
        job.setCostTime(result.costTime);
        job.setFusionCount(result.fusionCount);
        job.setStatus(JobStatus.success);
        job.setMessage("success");
        job.setUpdatedTimeNow();
        job.setResultFilePath(result.resultFile.getAbsolutePath());

        job.save();
    }
}
