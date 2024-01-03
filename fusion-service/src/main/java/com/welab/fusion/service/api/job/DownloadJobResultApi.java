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
package com.welab.fusion.service.api.job;

import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author zane.luo
 * @date 2023/12/01
 */
@Api(path = "job/result/download", name = "下载求交结果", desc = "下载求交结果，结果为 CSV 文件。")
public class DownloadJobResultApi extends AbstractApi<DownloadJobResultApi.Input, ResponseEntity<?>> {
    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(DownloadJobResultApi.Input input) throws Exception {
        JobDbModel job = jobService.findById(input.id);
        File file = job.getResultFile();
        if (file == null) {
            return fail("交结果文件已被删除");
        }

        return file(file);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        public String id;
    }
}
