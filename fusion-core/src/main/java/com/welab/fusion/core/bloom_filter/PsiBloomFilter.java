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
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.file.compression.impl.Zip;
import com.welab.wefe.common.util.JObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author zane.luo
 * @date 2023/11/8
 */
public class PsiBloomFilter {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String META_FILE_NAME = "PsiBloomFilter.json";
    private static final String DATA_FILE_NAME = "PsiBloomFilter.data";
    private static final String ZIP_FILE_NAME = "PsiBloomFilter.zip";
    public String id;
    public HashConfig hashConfig;
    public RsaPsiParam rsaPsiParam;
    /**
     * 过滤器中已插入的元素数量
     */
    public long insertedElementCount;
    @JSONField(serialize = false)
    private BloomFilter<String> bloomFilter;
    /**
     * 文件所在目录，用于从磁盘中加载时使用。
     */
    private Path dir;

    /**
     * 从磁盘中加载 PsiBloomFilter 对象
     */
    public static PsiBloomFilter of(Path dir) {
        // 加载元数据
        File metaFile = dir.resolve(META_FILE_NAME).toFile();
        String json = FileUtil.readString(metaFile, StandardCharsets.UTF_8);
        PsiBloomFilter result = JSON.parseObject(json).toJavaObject(PsiBloomFilter.class);
        result.rsaPsiParam.preproccess();
        result.dir = dir;
        return result;
    }

    public static File getDataFile(Path dir) {
        return dir.resolve(DATA_FILE_NAME).toFile();
    }

    /**
     * 为避免资源浪费，过滤器文件在需要用到的时候才加载。
     */
    private synchronized void loadDataFile() {
        if (this.bloomFilter != null) {
            return;
        }

        // 加载过滤器
        File bfFile = getDataFile();
        try (FileInputStream inputStream = new FileInputStream(bfFile)) {
            this.bloomFilter = BloomFilter.readFrom(
                    inputStream,
                    Funnels.stringFunnel(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getDataFile() {
        return dir.resolve(DATA_FILE_NAME).toFile();
    }

    public static PsiBloomFilter of(String id, HashConfig hashConfig, RsaPsiParam rsaPsiParam, BloomFilter<String> bloomFilter) {
        PsiBloomFilter psiBloomFilter = new PsiBloomFilter();
        psiBloomFilter.id = id;
        psiBloomFilter.hashConfig = hashConfig;
        psiBloomFilter.rsaPsiParam = rsaPsiParam;
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

    public boolean contains(BigInteger x) {
        return getBloomFilter().mightContain(x.toString());
    }

    /**
     * 将 meta 文件与 data 文件打包成 zip 文件
     *
     * 这里要注意： meta 文件中的 RsaPsiParam 对象包含私钥信息，打包前要删除。
     */
    public File zip() throws IOException {
        Path dir = FileSystem.PsiBloomFilter.getPath(id);
        File zipFile = dir.resolve(ZIP_FILE_NAME).toFile();

        // 已存在，不反复压缩。
        if (zipFile.exists()) {
            return zipFile;
        }

        // 抹除私钥信息，只保留公钥信息。
        // 为了避免影响旧对象，先拷贝一个新的。
        PsiBloomFilter newPsiBloomFilter = JObject.create(this).toJavaObject(PsiBloomFilter.class);
        newPsiBloomFilter.rsaPsiParam.cleanPrivateKey();

        // 抹除后输出到临时目录
        File tempMetaFile = dir.resolve("temp").resolve(META_FILE_NAME).toFile();
        tempMetaFile.getParentFile().mkdirs();
        Files.write(tempMetaFile.toPath(), JSON.toJSONBytes(newPsiBloomFilter));

        // 压缩文件
        File dataFile = dir.resolve(DATA_FILE_NAME).toFile();
        Zip.to(zipFile, tempMetaFile, dataFile);

        return zipFile;
    }

    public BloomFilter<String> getBloomFilter() {
        if (bloomFilter == null) {
            loadDataFile();
        }
        return bloomFilter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashConfig, rsaPsiParam, bloomFilter);
    }
}
