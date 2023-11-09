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
package com.welab.fusion.core.bloom_filter;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.wefe.common.TimeSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/8
 */
public class PsiBloomFilter {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String META_FILE_NAME = "PsiBloomFilter.json";
    private static final String DATA_FILE_NAME = "PsiBloomFilter.data";
    public List<HashConfig> hashConfigs;
    public RsaPsiParam rsaPsiParam;
    /**
     * 过滤器中已插入的元素数量
     */
    public long insertedElementCount;
    @JSONField(serialize = false)
    public BloomFilter<String> bloomFilter;

    /**
     * 从磁盘中加载 PsiBloomFilter 对象
     */
    public static PsiBloomFilter of(Path dir) {
        // 加载元数据
        File metaFile = dir.resolve(META_FILE_NAME).toFile();
        String json = FileUtil.readString(metaFile, StandardCharsets.UTF_8);
        PsiBloomFilter result = JSON.parseObject(json).toJavaObject(PsiBloomFilter.class);

        // 加载过滤器
        File bfFile = dir.resolve(DATA_FILE_NAME).toFile();
        try (FileInputStream inputStream = new FileInputStream(bfFile)) {
            result.bloomFilter = BloomFilter.readFrom(inputStream, Funnels.stringFunnel(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static PsiBloomFilter of(List<HashConfig> hashConfigs, RsaPsiParam rsaPsiParam, long insertedElementCount, BloomFilter<String> bloomFilter) {
        PsiBloomFilter psiBloomFilter = new PsiBloomFilter();
        psiBloomFilter.hashConfigs = hashConfigs;
        psiBloomFilter.rsaPsiParam = rsaPsiParam;
        psiBloomFilter.insertedElementCount = insertedElementCount;
        psiBloomFilter.bloomFilter = bloomFilter;
        return psiBloomFilter;
    }

    /**
     * 将 pis 布隆过滤器持久化到硬盘
     */
    public void sink(Path dir) {
        LOG.info("start to sink PsiBloomFilter to disk. dir:{}", dir);
        long start = System.currentTimeMillis();
        dir.toFile().mkdirs();

        // 输出过滤器
        File bfFile = dir.resolve(DATA_FILE_NAME).toFile();
        try (FileOutputStream outputStream = new FileOutputStream(bfFile)) {
            bloomFilter.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 输出元数据
        File metaFile = dir.resolve(META_FILE_NAME).toFile();
        try {
            Files.write(metaFile.toPath(), JSON.toJSONBytes(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long spend = System.currentTimeMillis() - start;
        LOG.info("sink PsiBloomFilter to disk success({}). dir:{}", TimeSpan.fromMs(spend), dir);
    }
}
