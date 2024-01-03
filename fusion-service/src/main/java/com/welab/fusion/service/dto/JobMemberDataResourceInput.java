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
package com.welab.fusion.service.dto;

import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.service.api.job.CreateJobApi;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;

import java.util.LinkedHashSet;

/**
 * @author zane.luo
 * @date 2023/11/28
 */
public class JobMemberDataResourceInput extends AbstractApiInput {
    @Check(name = "数据量", donotShow = true)
    public long totalDataCount = -1;

    @Check(name = "资源类型", require = true, desc = "数据源类型：数据集、过滤器")
    public DataResourceType dataResourceType;

    @Check(name = "输入的过滤器信息")
    public CreateJobApi.BloomFilterResourceInput bloomFilterResourceInput;
    @Check(name = "输入的数据集信息")
    public CreateJobApi.TableDataResourceInput tableDataResourceInfo;

    @Check(name = "主键 hash 方案", require = true)
    public HashConfig hashConfig;

    @Check(name = "附加结果字段")
    public LinkedHashSet<String> additionalResultColumns;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        // 去重，主键相关的字段不应该出现在附加结果字段中。
        if (additionalResultColumns != null) {
            LinkedHashSet<String> idHeaders = hashConfig.getIdHeadersForCsv();
            for (String column : idHeaders) {
                additionalResultColumns.remove(column);
            }
        }

    }
}
