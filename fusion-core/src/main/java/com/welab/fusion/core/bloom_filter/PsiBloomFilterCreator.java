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

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.NamedThreadFactory;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.wefe.common.crypto.Rsa;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 生成用于 PSI（Private Set Intersection） 的布隆过滤器
 *
 * @author zane.luo
 * @date 2023/11/8
 */
public class PsiBloomFilterCreator implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private AbstractTableDataSourceReader dataSourceReader;
    public List<HashConfig> hashConfigs;
    private RsaPsiParam rsaPsiParam;
    private final BloomFilter<String> bloomFilter;
    /**
     * 用于生成过滤器的线程池
     */
    private final ThreadPoolExecutor GENERATE_FILTER_THREAD_POOL;

    public PsiBloomFilterCreator(AbstractTableDataSourceReader dataSourceReader, List<HashConfig> hashConfigs) {
        this.hashConfigs = hashConfigs;
        this.dataSourceReader = dataSourceReader;

        Rsa.RsaKeyPair keyPair = Rsa.generateKeyPair();
        this.rsaPsiParam = RsaPsiParam.of(keyPair);

        this.bloomFilter = createBloomFilter(dataSourceReader);
        GENERATE_FILTER_THREAD_POOL = createThreadPoll();
    }

    /**
     * 创建线程池，将加密动作并行。
     */
    private ThreadPoolExecutor createThreadPoll() {
        /**
         * 这里经过了大量测试
         * 在华为云测试环境（6物理核6逻辑核），池大小为 2 时最快。
         * 在 Mac M1，（4物理核4逻辑核），池大小为 6 时最快。
         */
        // 默认
        int pollSize = Runtime.getRuntime().availableProcessors() / 2;
        // mac
        if (OS.get() == OS.mac) {
            pollSize = Runtime.getRuntime().availableProcessors() - 2;
        }

        return new ThreadPoolExecutor(
                pollSize,
                pollSize,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("generate-filter-thread-pool-", true)
        );
    }

    /**
     * 创建布隆过滤器
     */
    private BloomFilter<String> createBloomFilter(AbstractTableDataSourceReader dataSourceReader) {
        /**
         * 布隆过滤器需要指定一个预估的数据量，但是这个数据量不是实际拥有的数据量。
         * 而是在实际的业务场景中世界样本的总量。
         *
         * 由于我们的场景大多是用户的手机号、身份证号等，这些数据的总量大约是十亿。
         * 综合考虑，这个预估值设定在一亿。
         */
        int minCount = 100_000_000;
        long count = dataSourceReader.getTotalDataRowCount();

        long expectedInsertions = Math.max(count, minCount);
        return BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), expectedInsertions, 0.0001);
    }

    /**
     * 从数据源读取数据，加密，写入布隆过滤器。
     */
    public PsiBloomFilter create() throws StatusCodeWithException {
        dataSourceReader.readAll(new BiConsumer<Long, LinkedHashMap<String, Object>>() {
            @Override
            public void accept(Long index, LinkedHashMap<String, Object> row) {
                String key = hashConfigs.stream().map(x -> x.hash(row)).collect(Collectors.joining());

                try {
                    BigInteger z = CompletableFuture.supplyAsync(
                            () -> encrypt(key),
                            GENERATE_FILTER_THREAD_POOL
                    ).get();
                    bloomFilter.put(z.toString());
                } catch (Exception e) {
                    LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                }
            }
        });

        return PsiBloomFilter.of(hashConfigs, rsaPsiParam, dataSourceReader.getReadDataRows(), bloomFilter);
    }

    /**
     * 对主键进行加密
     */
    private BigInteger encrypt(String key) {
        BigInteger h = Convert.toBigInteger(key);

        BigInteger rp = h.modPow(rsaPsiParam.getEp(), rsaPsiParam.privatePrimeP);
        BigInteger rq = h.modPow(rsaPsiParam.getEq(), rsaPsiParam.privatePrimeQ);
        BigInteger z = (rp.multiply(rsaPsiParam.getCp()).add(rq.multiply(rsaPsiParam.getCq()))).remainder(rsaPsiParam.publicModulus);
        return z;
    }


    @Override
    public void close() throws IOException {
        GENERATE_FILTER_THREAD_POOL.shutdown();
        try {
            GENERATE_FILTER_THREAD_POOL.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
        dataSourceReader.close();
    }


    /**
     * 测试
     */
    public static void main(String[] args) throws IOException, StatusCodeWithException {
        File file = new File("D:\\data\\wefe\\ivenn_10w_20210319_vert_promoter.csv");
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file);
        System.out.println(reader.getHeader());
        List<HashConfig> hashConfigs = Arrays.asList(
                HashConfig.of(HashMethod.MD5, "id")
        );

        Path dir = Paths.get(file.getParent()).resolve("test-bf");

        // 生成过滤器
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator(reader, hashConfigs)) {
            PsiBloomFilter psiBloomFilter = creator.create();
            psiBloomFilter.sink(dir);
        }

        // 加载过滤器
        PsiBloomFilter psiBloomFilter = PsiBloomFilter.of(dir);
        System.out.println(psiBloomFilter.insertedElementCount);
    }
}
