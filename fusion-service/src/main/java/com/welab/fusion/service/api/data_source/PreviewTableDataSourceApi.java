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
package com.welab.fusion.service.api.data_source;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.data_source.ExcelTableDataSourceReader;
import com.welab.fusion.core.data_source.SqlTableDataSourceReader;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.config.fastjson.BlockForPartnerField;
import com.welab.fusion.service.constans.AddMethod;
import com.welab.fusion.service.service.BloomFilterService;
import com.welab.wefe.common.crypto.Md5;
import com.welab.wefe.common.data.source.JdbcDataSourceClient;
import com.welab.wefe.common.data.source.SuperDataSourceClient;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author zane.luo
 * @date 2023/11/17
 */
@Api(path = "data_source/preview", name = "预览数据")
public class PreviewTableDataSourceApi extends AbstractApi<PreviewTableDataSourceApi.Input, PreviewTableDataSourceApi.Output> {
    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<PreviewTableDataSourceApi.Output> handle(PreviewTableDataSourceApi.Input input) throws Exception {
        Output output = bloomFilterService.previewTableDataSource(input);
        return success(output);
    }

    public static class Input extends SaveDataSourceApi.Input {
        @Check(require = true)
        public AddMethod addMethod;

        @Secret(maskStrategy = MaskStrategy.BLOCK)
        @Check(name = "sql脚本", blockXss = false, oneSelectSql = true)
        public String sql;

        @Check(name = "数据源文件")
        @BlockForPartnerField
        public String dataSourceFile;

        @JSONField(serialize = false)
        public File getFile() {
            if (addMethod == AddMethod.LocalFile) {
                return new File(dataSourceFile);
            } else {
                return FileSystem.getTempDir().resolve(dataSourceFile).toFile();
            }
        }

        /**
         * 生成名称，供自动生成过滤器时使用。
         */
        @JSONField(serialize = false)
        public String buildAutoName() {
            if (addMethod == AddMethod.Database) {
                return databaseType + "_" + Md5.of(JSON.toJSONString(dataSourceParams) + sql);
            } else {
                return FileUtil.getFileNameWithoutSuffix(dataSourceFile);
            }
        }

        /**
         * 生成描述，供自动生成过滤器时使用。
         */
        @JSONField(serialize = false)
        public String buildAutoDescription() {
            if (addMethod == AddMethod.Database) {
                return databaseType + ": " + sql;
            } else {
                return "File: " + dataSourceFile;
            }
        }

        public JdbcDataSourceClient createJdbcClient() {
            if (addMethod != AddMethod.Database) {
                throw new UnsupportedOperationException();
            }
            return SuperDataSourceClient.create(databaseType.name(), dataSourceParams);
        }

        public AbstractTableDataSourceReader createReader(long maxReadRows, long maxReadTimeInMs) throws Exception {
            switch (addMethod) {
                case Database:
                    JdbcDataSourceClient client = createJdbcClient();
                    client.test();

                    return new SqlTableDataSourceReader(client, sql, maxReadRows, maxReadTimeInMs);

                default:
                    File file = getFile();

                    if (!file.exists()) {
                        throw new RuntimeException("未找到文件:" + file.getAbsolutePath());
                    }

                    boolean isCsv = file.getName().endsWith("csv");
                    return isCsv
                            ? new CsvTableDataSourceReader(file, maxReadRows, maxReadTimeInMs)
                            : new ExcelTableDataSourceReader(file, maxReadRows, maxReadTimeInMs);
            }
        }
    }

    public static class Output {
        @Check(name = "字段列表")
        public List<String> header;
        @Check(name = "原始数据列表")
        public List<Map<String, Object>> rows;
    }
}
