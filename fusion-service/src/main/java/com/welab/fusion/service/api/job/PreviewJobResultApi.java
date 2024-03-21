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

import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.util.Constant;
import com.welab.fusion.service.api.data_source.PreviewTableDataSourceApi;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.service.JobMemberService;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/12/01
 */
@Api(path = "job/result/preview", name = "预览求交结果")
public class PreviewJobResultApi extends AbstractApi<PreviewJobResultApi.Input, PreviewTableDataSourceApi.Output> {
    @Autowired
    private JobService jobService;
    @Autowired
    private JobMemberService jobMemberService;

    @Override
    protected ApiResult<PreviewTableDataSourceApi.Output> handle(PreviewJobResultApi.Input input) throws Exception {
        JobDbModel job = jobService.findById(input.id);
        File file = job.getResultFile();
        if (file == null) {
            return success();
        }

        int rows = 50;
        try (CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file, rows, -1)) {
            PreviewTableDataSourceApi.Output output = new PreviewTableDataSourceApi.Output();
            output.header = reader.getHeader();
            output.rows = new ArrayList<>(rows);

            reader.readRows((index, row) -> output.rows.add(row));
            return success(output);
        }
    }

    private List<String> renderHeader(JobDbModel job, List<String> header) {
        HashConfig hashConfig = jobMemberService.findMyself(job.getId()).getHashConfigModel();
        return header.stream()
                .map(x -> {
                    if (Constant.KEY_COLUMN_NAME.equals(x)) {
                        return Constant.KEY_COLUMN_NAME + "[" + hashConfig + "]";
                    } else {
                        return x;
                    }
                })
                .collect(Collectors.toList());
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        public String id;
    }
}
