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

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadUtil;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.psi.PsiUtils;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.crypto.Rsa;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;

/**
 * 生成用于 PSI（Private Set Intersection） 的布隆过滤器
 *
 * @author zane.luo
 * @date 2023/11/8
 */
public class PsiBloomFilterCreator implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    /**
     * 最小的预估数据量
     */
    public static int MIN_EXPECTED_INSERTIONS = 100_000_000;
    private AbstractTableDataSourceReader dataSourceReader;
    public HashConfig hashConfig;
    private RsaPsiParam rsaPsiParam;
    private final BloomFilter<String> bloomFilter;
    /**
     * 用于生成过滤器的线程池
     */
    private final ThreadPoolExecutor GENERATE_FILTER_THREAD_POOL;

    public PsiBloomFilterCreator(AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig) {
        this.hashConfig = hashConfig;
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
        int pollSize = Runtime.getRuntime().availableProcessors() - 2;

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
         * 综合考虑，这个预估值设定最小为一亿。
         */
        int minCount = MIN_EXPECTED_INSERTIONS;
        int maxCount = Integer.MAX_VALUE;
        long count = dataSourceReader.getTotalDataRowCount() * 2;

        long expectedInsertions = Math.max(count, minCount);
        // 不能超过上限
        if (expectedInsertions > maxCount) {
            expectedInsertions = maxCount;
        }

        return BloomFilter.create(
                Funnels.stringFunnel(Charsets.UTF_8),
                expectedInsertions,
                0.0001
        );
    }

    public PsiBloomFilter create(String id) throws Exception {
        return create(id, null);
    }

    /**
     * 从数据源读取数据，加密，写入布隆过滤器。
     *
     * @param progressConsumer 进度回调
     */
    public PsiBloomFilter create(String id, BiConsumer<Long, Long> progressConsumer) throws Exception {
        LOG.info("start to create PsiBloomFilter. id:{}", id);
        final long start = System.currentTimeMillis();
        LongAdder insertedElementCount = new LongAdder();
        AtomicReference<Exception> error = new AtomicReference<>();
        ThreadPool singleThreadExecutor = new ThreadPool("create-psi_bloom_filter:" + id, 1);
        singleThreadExecutor.run(() -> {
            try {
                dataSourceReader.readRows(new BiConsumer<Long, LinkedHashMap<String, Object>>() {
                    @Override
                    public void accept(Long index, LinkedHashMap<String, Object> row) {

                        // 避免读取的数据堆积在内存
                        while (GENERATE_FILTER_THREAD_POOL.getQueue().size() > 10000) {
                            ThreadUtil.safeSleep(100);
                        }

                        // 这一句如果放在异步线程会导致性能大幅下降，原因不详。
                        String key = hashConfig.hash(row);

                        try {
                            CompletableFuture.runAsync(
                                    () -> {
                                        BigInteger z = encrypt(key);
                                        bloomFilter.put(z.toString());
                                        insertedElementCount.increment();
                                    },
                                    GENERATE_FILTER_THREAD_POOL
                            );

                        } catch (Exception e) {
                            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                        }
                    }
                });
            } catch (StatusCodeWithException e) {
                error.set(e);
            }
        });

        long lastProgress = 0;
        while (
            // 未读取完毕
                !dataSourceReader.isFinished()
                        // 队列未消费完
                        || GENERATE_FILTER_THREAD_POOL.getQueue().size() > 0
                        // 线程还在工作中
                        || GENERATE_FILTER_THREAD_POOL.getActiveCount() > 0
        ) {
            ThreadUtil.safeSleep(1000);

            long progress = insertedElementCount.longValue();
            long speed = progress - lastProgress;
            lastProgress = progress;

            // 更新进度
            if (progressConsumer != null) {
                progressConsumer.accept(progress, speed);
            }

            if (error.get() != null) {
                throw error.get();
            }
        }

        long spend = System.currentTimeMillis() - start;
        LOG.info("create PsiBloomFilter success({}). id:{}", TimeSpan.fromMs(spend), id);

        return PsiBloomFilter.of(
                id,
                hashConfig,
                rsaPsiParam,
                insertedElementCount.longValue(),
                bloomFilter
        );
    }

    /**
     * 对主键进行加密
     */
    private BigInteger encrypt(String key) {
        BigInteger h = PsiUtils.stringToBigInteger(key);

        BigInteger rp = h.modPow(rsaPsiParam.getEp(), rsaPsiParam.privatePrimeP);
        BigInteger rq = h.modPow(rsaPsiParam.getEq(), rsaPsiParam.privatePrimeQ);
        BigInteger z = (rp.multiply(rsaPsiParam.getCp())
                .add(rq.multiply(rsaPsiParam.getCq())))
                .remainder(rsaPsiParam.publicModulus);
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
    public static void main(String[] args) throws Exception {
        // MIN_EXPECTED_INSERTIONS = 1_000_000_000;
        File file = new File("D:\\data\\wefe\\ivenn_10w_20210319_vert_promoter.csv");
        // File file = new File("D:\\data\\wefe\\3x100000000rows-miss0-auto_increment.csv");
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file);
        System.out.println(reader.getHeader());
        HashConfig hashConfig = HashConfig.of(HashConfigItem.of(HashMethod.MD5, "id"));

        // 生成过滤器
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator(reader, hashConfig)) {
            PsiBloomFilter psiBloomFilter = creator.create("test", (progress, speed) -> {
                System.out.println("进度：" + progress + "," + speed + "条/秒");
            });

            // Path dir = Paths.get(file.getParent()).resolve("test-bf");
            // psiBloomFilter.sink(dir);
        }

        System.out.println("end");
    }
}
