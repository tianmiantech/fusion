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
package com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadUtil;
import com.welab.fusion.core.progress.Progress;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
public class PsiECEncryptedDataConsumer implements BiConsumer<Long, LinkedHashMap<String, Object>>, Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private PsiECEncryptedData psiECEncryptedData;
    /**
     * 已经写入过滤器的数据量
     */
    private LongAdder insertedElementCount = new LongAdder();
    /**
     * 用于生成过滤器的线程池
     */
    private final ThreadPoolExecutor THREAD_POOL;
    private Progress progress;
    /**
     * 保存加密后的数据的文件
     * 务必使用线程安全的写入方式
     */
    private BufferedWriter fileWriter;
    private AtomicReference<Exception> error = new AtomicReference<>(null);

    public PsiECEncryptedDataConsumer(PsiECEncryptedData psiECEncryptedData, Progress progress) throws IOException {
        this.psiECEncryptedData = psiECEncryptedData;
        this.THREAD_POOL = createThreadPoll();
        this.progress = progress;

        File file = psiECEncryptedData.getDataFile();
        this.fileWriter = FileUtil.buildBufferedWriter(file, false);

        // 写入列头
        this.fileWriter.write(
                Constant.INDEX_COLUMN_NAME + ","
                        + Constant.KEY_COLUMN_NAME
                        + System.lineSeparator()
        );
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
        while (THREAD_POOL.getQueue().size() > 1000) {
            ThreadUtil.safeSleep(100);
        }

        THREAD_POOL.execute(() -> {
            String key = psiECEncryptedData.hashConfig.hash(row);
            String encrypted = psiECEncryptedData.encryptMyselfData(key);
            // 将加密后的数据保存到文件
            try {

                /**
                 * 后续求交得到的交集是密文
                 * 需要根据 index 找到对应的明文数据
                 * 所以这里必须把映射关系存起来
                 */
                Object indexValue = row.get(Constant.INDEX_COLUMN_NAME);
                if (indexValue == null) {
                    indexValue = index;
                }

                String line = indexValue + "," + encrypted + System.lineSeparator();

                // 这里必须拼接好整行之后一次性写入，如果分开写入，在并发的影响下数据会错乱。
                this.fileWriter.append(line);
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                error.set(e);
            }
            insertedElementCount.increment();
        });

    }

    public LongAdder getInsertedElementCount() {
        return insertedElementCount;
    }

    public boolean isWorking() {
        return !getQueue().isEmpty() || THREAD_POOL.getActiveCount() > 0;
    }

    public BlockingQueue<Runnable> getQueue() {
        return THREAD_POOL.getQueue();
    }

    public int getActiveCount() {
        return THREAD_POOL.getActiveCount();
    }

    @Override
    public void close() throws IOException {
        THREAD_POOL.shutdownNow();
        fileWriter.close();
    }

    public void refreshProgress() {
        progress.updateCompletedWorkload(getInsertedElementCount().longValue());
    }

    public Progress getProgress() {
        return progress;
    }

    public Exception getError() {
        return error.get();
    }
}
