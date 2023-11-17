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
package com.welab.fusion.service.service;

import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.data_source.ExcelTableDataSourceReader;
import com.welab.fusion.core.data_source.SqlTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.bloom_filter.AddBloomFilterApi;
import com.welab.fusion.service.api.bloom_filter.PreviewTableDataSourceApi;
import com.welab.fusion.service.constans.BloomFilterAddMethod;
import com.welab.fusion.service.database.entity.BloomFilterDbModel;
import com.welab.fusion.service.model.Progress;
import com.welab.fusion.service.model.ProgressManager;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.data.source.JdbcDataSourceClient;
import com.welab.wefe.common.data.source.SuperDataSourceClient;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;


/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Service
public class BloomFilterService extends AbstractService {

    @Autowired
    private DataSourceService dataSourceService;

    public Progress add(AddBloomFilterApi.Input input) throws Exception {
        AbstractTableDataSourceReader reader = createReader(input, -1, -1);

        BloomFilterDbModel model = new BloomFilterDbModel();
        model.setName(input.name);
        model.setAddMethod(input.addMethod);
        model.setDescription(input.description);
        model.setSql(input.sql);
        model.setHashConfigs(input.hashConfig.toJson());


        Progress progress = ProgressManager.startNew(model.getId());

        CommonThreadPool.run(() -> {
            try {
                create(model, progress, reader, input.hashConfig);
                model.save();
                progress.success();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                progress.failed(e);
            }
        });


        return progress;
    }

    private void create(BloomFilterDbModel model, Progress progress, AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig) throws IOException, StatusCodeWithException {

        progress.setMessage("正在统计数据总量...");
        progress.updateTotalWorkload(dataSourceReader.getTotalDataRowCount());

        progress.setMessage("正在生成过滤器...");
        // 生成过滤器
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator(dataSourceReader, hashConfig)) {
            PsiBloomFilter psiBloomFilter = creator.create(model.getId(), (index) -> {
                progress.updateCompletedWorkload(index);
            });

            progress.setMessage("过滤器生成完毕，正在储存...");
            Path sinkDir = FileSystem.PsiBloomFilter.getPath(model.getId());
            psiBloomFilter.sink(sinkDir);

            // 填充 model
            model.setTotalDataCount(dataSourceReader.getTotalDataRowCount());
            model.setStorageDir(sinkDir.toAbsolutePath().toString());
            model.setStorageSize(PsiBloomFilter.getDataFile(sinkDir).length());
        }
    }

    public AbstractTableDataSourceReader createReader(PreviewTableDataSourceApi.Input input, long maxReadRows, long maxReadTimeInMs) throws Exception {
        switch (input.addMethod) {
            case Database:
                JdbcDataSourceClient client = StringUtil.isNotEmpty(input.dataSourceId)
                        ? dataSourceService.findById(input.dataSourceId).getJdbcDataSourceClient()
                        : SuperDataSourceClient.create(input.databaseType.name(), input.dataSourceParams);

                client.test();

                return new SqlTableDataSourceReader(client, input.sql, maxReadRows, maxReadTimeInMs);

            default:
                File file;
                if (input.addMethod == BloomFilterAddMethod.LocalFile) {
                    file = new File(input.dataSourceFile);
                } else {
                    file = FileSystem.getTempDir().resolve(input.dataSourceFile).toFile();
                }

                if (!file.exists()) {
                    throw new RuntimeException("未找到文件:" + file.getAbsolutePath());
                }

                boolean isCsv = file.getName().endsWith("csv");
                return isCsv
                        ? new CsvTableDataSourceReader(file, maxReadRows, maxReadTimeInMs)
                        : new ExcelTableDataSourceReader(file, maxReadRows, maxReadTimeInMs);
        }
    }

    /**
     * 预览数据源中的数据
     */
    public PreviewTableDataSourceApi.Output previewTableDataSource(PreviewTableDataSourceApi.Input input) throws Exception {
        AbstractTableDataSourceReader reader = createReader(input, 100, -1);


        PreviewTableDataSourceApi.Output output = new PreviewTableDataSourceApi.Output();
        output.header = reader.getHeader();
        output.rows = new ArrayList<>(100);

        reader.readRows((index, row) -> output.rows.add(row));
        return output;
    }
}
