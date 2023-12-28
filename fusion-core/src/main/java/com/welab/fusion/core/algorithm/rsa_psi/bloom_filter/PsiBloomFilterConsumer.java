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

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadUtil;
import com.welab.fusion.core.progress.Progress;
import com.welab.fusion.core.util.PsiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
public class PsiBloomFilterConsumer implements BiConsumer<Long, LinkedHashMap<String, Object>>, Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PsiBloomFilter psiBloomFilter;
    /**
     * 已经写入过滤器的数据量
     */
    private LongAdder insertedElementCount = new LongAdder();
    /**
     * 用于生成过滤器的线程池
     */
    private final ThreadPoolExecutor GENERATE_FILTER_THREAD_POOL;
    private Progress progress;

    public PsiBloomFilterConsumer(PsiBloomFilter psiBloomFilter, Progress progress) {
        this.psiBloomFilter = psiBloomFilter;
        GENERATE_FILTER_THREAD_POOL = createThreadPoll();
        this.progress = progress;
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

    @Override
    public void accept(Long index, LinkedHashMap<String, Object> row) {

        // 避免读取的数据堆积在内存
        while (GENERATE_FILTER_THREAD_POOL.getQueue().size() > 1000) {
            ThreadUtil.safeSleep(100);
        }

        try {
            String key = psiBloomFilter.hashConfig.hash(row);
            GENERATE_FILTER_THREAD_POOL.execute(() -> {
                BigInteger z = encrypt(key);
                psiBloomFilter.getBloomFilter().put(z.toString());
                insertedElementCount.increment();
            });
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
    }

    /**
     * 对主键进行加密
     */
    private BigInteger encrypt(String key) {
        RsaPsiParam rsaPsiParam = psiBloomFilter.rsaPsiParam;
        BigInteger h = PsiUtils.stringToBigInteger(key);

        BigInteger rp = h.modPow(rsaPsiParam.getEp(), rsaPsiParam.privatePrimeP);
        BigInteger rq = h.modPow(rsaPsiParam.getEq(), rsaPsiParam.privatePrimeQ);
        BigInteger z = (rp.multiply(rsaPsiParam.getCp())
                .add(rq.multiply(rsaPsiParam.getCq())))
                .remainder(rsaPsiParam.publicModulus);
        return z;
    }

    public LongAdder getInsertedElementCount() {
        return insertedElementCount;
    }

    public boolean isWorking() {
        return !getQueue().isEmpty() || GENERATE_FILTER_THREAD_POOL.getActiveCount() > 0;
    }

    public BlockingQueue<Runnable> getQueue() {
        return GENERATE_FILTER_THREAD_POOL.getQueue();
    }

    public int getActiveCount() {
        return GENERATE_FILTER_THREAD_POOL.getActiveCount();
    }

    @Override
    public void close() throws IOException {
        GENERATE_FILTER_THREAD_POOL.shutdownNow();
    }

    public void refreshProgress() {
        progress.updateCompletedWorkload(getInsertedElementCount().longValue());
    }

    public Progress getProgress() {
        return progress;
    }
}
