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
package com.welab.fusion.service.api.bloom_filter;

import com.welab.fusion.service.api.data_source.AddDataSourceApi;
import com.welab.fusion.service.constans.BloomFilterAddMethod;
import com.welab.fusion.service.service.BloomFilterService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author zane.luo
 * @date 2023/11/17
 */
@Api(path = "bloom_filter/preview_table_data_source", name = "预览数据")
public class PreviewTableDataSourceApi extends AbstractApi<PreviewTableDataSourceApi.Input, PreviewTableDataSourceApi.Output> {
    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<PreviewTableDataSourceApi.Output> handle(PreviewTableDataSourceApi.Input input) throws Exception {
        Output output = bloomFilterService.previewTableDataSource(input);
        return success(output);
    }

    public static class Input extends AddDataSourceApi.Input {
        @Check(require = true)
        public BloomFilterAddMethod addMethod;

        @Check(name = "数据源id")
        public String dataSourceId;
        @Check(name = "sql脚本", blockXss = false, oneSelectSql = true)
        public String sql;

        @Check(name = "数据源文件")
        public String dataSourceFile;
    }

    public static class Output {
        @Check(name = "字段列表")
        public List<String> header;
        @Check(name = "原始数据列表")
        public List<Map<String, Object>> rows;
    }
}
