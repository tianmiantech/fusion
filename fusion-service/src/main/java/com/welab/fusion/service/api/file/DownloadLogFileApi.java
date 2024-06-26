/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.fusion.service.api.file;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.io.File;

@Api(path = "file/download_log", name = "下载日志文件")
public class DownloadLogFileApi extends AbstractApi<DownloadLogFileApi.Input, ResponseEntity<?>> {
    @Value("${logging.file.name:}")
    private String logFile;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {
        return file(new File(logFile));
    }

    public static class Input extends AbstractApiInput {

    }

}


