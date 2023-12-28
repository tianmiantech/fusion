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
package com.welab.fusion.core.algorithm.rsa_psi.bloom_filter;

import cn.hutool.core.thread.ThreadUtil;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.welab.fusion.core.progress.Progress;
import com.welab.fusion.core.io.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.crypto.Rsa;
import com.welab.wefe.common.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
    private PsiBloomFilter psiBloomFilter;
    /**
     * 用来异步生成过滤器的线程池
     */
    private ThreadPool singleThreadExecutor;
    private Progress progress;

    public PsiBloomFilterCreator(String id, AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig) {
        this(
                id,
                dataSourceReader,
                hashConfig,
                Progress.of(id, dataSourceReader.getTotalDataRowCount())
        );
    }

    public PsiBloomFilterCreator(String id, AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig, Progress progress) {
        this.dataSourceReader = dataSourceReader;
        this.singleThreadExecutor = new ThreadPool("create-psi_bloom_filter:" + id, 1);

        this.progress = progress;

        this.psiBloomFilter = PsiBloomFilter.of(
                id,
                hashConfig,
                RsaPsiParam.of(Rsa.generateKeyPair()),
                createBloomFilter(dataSourceReader)
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

    public PsiBloomFilter create() throws Exception {
        return create(null);
    }

    /**
     * 从数据源读取数据，加密，写入布隆过滤器。
     *
     * @param progressConsumer 进度回调
     */
    public PsiBloomFilter create(Consumer<Progress> progressConsumer) throws Exception {
        LOG.info("start to create PsiBloomFilter. id:{}", psiBloomFilter.id);
        final long start = System.currentTimeMillis();

        AtomicReference<Exception> error = new AtomicReference<>();
        try (PsiBloomFilterConsumer consumer = new PsiBloomFilterConsumer(psiBloomFilter, progress)) {
            singleThreadExecutor.execute(() -> {
                try {
                    dataSourceReader.readRows(consumer);
                } catch (Exception e) {
                    error.set(e);
                }
            });
            singleThreadExecutor.shutdown();

            // 等待：未读取完毕 or 线程还在工作中
            while (!dataSourceReader.isFinished() || consumer.isWorking()) {
                ThreadUtil.safeSleep(1000);

                consumer.refreshProgress();

                // 更新进度
                if (progressConsumer != null) {
                    progressConsumer.accept(consumer.getProgress());
                }

                if (error.get() != null) {
                    throw error.get();
                }
            }

            psiBloomFilter.insertedElementCount = consumer.getInsertedElementCount().longValue();
        }

        long spend = System.currentTimeMillis() - start;
        LOG.info("create PsiBloomFilter success({}). id:{}", TimeSpan.fromMs(spend), psiBloomFilter.id);

        return psiBloomFilter;
    }

    public Progress getProgress() {
        return progress;
    }

    @Override
    public void close() throws IOException {
        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdownNow();
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
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator("test", reader, hashConfig)) {
            PsiBloomFilter psiBloomFilter = creator.create(progress -> {
                System.out.println("进度：" + progress.getCompletedWorkload() + "," + progress.getSpeedInSecond() + "条/秒");
            });

            // Path dir = Paths.get(file.getParent()).resolve("test-bf");
            // psiBloomFilter.sink(dir);
        }

        System.out.println("end");
    }


}
