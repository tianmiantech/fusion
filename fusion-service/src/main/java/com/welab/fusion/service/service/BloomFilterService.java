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

import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.progress.Progress;
import com.welab.fusion.service.api.bloom_filter.AddBloomFilterApi;
import com.welab.fusion.service.api.bloom_filter.QueryBloomFilterApi;
import com.welab.fusion.service.api.bloom_filter.UpdateBloomFilterApi;
import com.welab.fusion.service.api.data_source.PreviewTableDataSourceApi;
import com.welab.fusion.service.constans.AddMethod;
import com.welab.fusion.service.database.base.MySpecification;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.BloomFilterDbModel;
import com.welab.fusion.service.database.repository.BloomFilterRepository;
import com.welab.fusion.service.dto.base.PagingOutput;
import com.welab.fusion.service.dto.entity.BloomFilterOutputModel;
import com.welab.fusion.service.model.ProgressManager;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;


/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Service
public class BloomFilterService extends AbstractService {

    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private BloomFilterRepository bloomFilterRepository;

    public Progress add(AddBloomFilterApi.Input input) throws Exception {
        // 自动保存数据源信息
        dataSourceService.trySave(input);

        BloomFilterDbModel model = new BloomFilterDbModel();
        model.setName(input.name);
        model.setAddMethod(input.addMethod);
        model.setDescription(input.description);
        model.setSql(input.sql);
        model.setHashConfig(input.hashConfig.toJson());

        Progress progress = ProgressManager.startNew(model.getId());
        CommonThreadPool.run(() -> {
            try (AbstractTableDataSourceReader reader = input.createReader(-1, -1)) {
                create(model, progress, reader, input.hashConfig);
                bloomFilterRepository.save(model);
                progress.success();

                // 清理
                input.getFile().delete();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                progress.failed(e);
            }
        });

        return progress;
    }

    private void create(BloomFilterDbModel model, Progress progress, AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig) throws Exception {

        progress.setMessageAndLog("正在统计数据总量...");
        progress.updateTotalWorkload(dataSourceReader.getTotalDataRowCount());

        progress.setMessageAndLog("正在生成过滤器...");
        // 生成过滤器
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator(model.getId(), dataSourceReader, hashConfig, progress)) {

            PsiBloomFilter psiBloomFilter = creator.create();

            progress.setMessageAndLog("过滤器生成完毕，正在储存...");
            psiBloomFilter.sink();

            // 填充 model
            model.setTotalDataCount(dataSourceReader.getTotalDataRowCount());
            model.setStorageDir(psiBloomFilter.getDir().toAbsolutePath().toString());
            model.setStorageSize(psiBloomFilter.getDataFile().length());
            model.setKey(psiBloomFilter.hashCode() + "");
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 预览数据源中的数据
     */
    public PreviewTableDataSourceApi.Output previewTableDataSource(PreviewTableDataSourceApi.Input input) throws Exception {
        try (AbstractTableDataSourceReader reader = input.createReader(100, -1)) {

            PreviewTableDataSourceApi.Output output = new PreviewTableDataSourceApi.Output();
            output.header = reader.getHeader();
            output.rows = new ArrayList<>(100);

            reader.readRows((index, row) -> output.rows.add(row));
            return output;
        }


    }

    /**
     * 分页查询
     */
    public PagingOutput<BloomFilterOutputModel> query(QueryBloomFilterApi.Input input) {
        MySpecification<BloomFilterDbModel> where = Where.create()
                .contains("name", input.name)
                .build();
        return bloomFilterRepository.paging(where, input, BloomFilterOutputModel.class);
    }

    public BloomFilterDbModel findOneById(String id) {
        BloomFilterDbModel model = bloomFilterRepository.findById(id).orElse(null);
        if (model == null) {
            return null;
        }

        return model;
    }

    /**
     * 删除过滤器
     *
     * 1. 删除磁盘文件
     * 2. 删除数据库记录
     */
    public void delete(String id) {
        BloomFilterDbModel model = findOneById(id);

        // 删除整个文件夹
        File dir = new File(model.getStorageDir());
        FileUtil.deleteFileOrDir(dir);

        bloomFilterRepository.delete(model);
    }

    public void update(UpdateBloomFilterApi.Input input) {
        BloomFilterDbModel model = findOneById(input.id);
        model.setName(input.name);
        model.setDescription(input.description);
        model.setUpdatedTime(new Date());
        bloomFilterRepository.save(model);
    }

    public BloomFilterDbModel findAutoGenerateByKey(int key) {
        MySpecification<BloomFilterDbModel> where = Where.create()
                .equal("addMethod", AddMethod.AutoGenerate)
                .equal("key", key)
                .build();
        return bloomFilterRepository.findOne(where).orElse(null);
    }
}
