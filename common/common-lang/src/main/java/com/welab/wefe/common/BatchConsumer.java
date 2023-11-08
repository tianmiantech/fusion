/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Batch consumption processor
 * <p>
 * Used to change processing from one by one to batch processing
 *
 * @author Zane
 */
public class BatchConsumer<T> implements AutoCloseable {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    /**
     * queue.size() 性能不好，用 AtomicInteger 额外储存队列长度。
     */
    private AtomicInteger queueSize = new AtomicInteger();
    /**
     * Is the consumption method being implemented
     */
    private boolean draining = false;
    /**
     * Last consumption time
     */
    private long lastConsumeTime;
    /**
     * Mark whether this mass consumption processor is in use
     * When it is no longer used, the consuming thread exits.
     */
    private AtomicBoolean inUse = new AtomicBoolean(true);

    /**
     * Maximum batch data
     */
    private int maxBatchSize;
    /**
     * Maximum consumption delay
     * <p>
     * ps: when the data production speed is greater than the consumption speed, the actual consumption delay will exceed this setting.
     */
    private final int maxDelayMs;
    /**
     * Data consumption method
     */
    private final Consumer<List<T>> consumer;

    public BatchConsumer(int maxBatchSize, int maxDelayMs, Consumer<List<T>> consumer) {
        this.maxBatchSize = maxBatchSize;
        this.maxDelayMs = maxDelayMs;
        this.consumer = consumer;

        startLoop();
    }

    public void add(T data) {

        if (data == null) {
            return;
        }

        if (!inUse.get()) {
            throw new RuntimeException("This BatchConsumer is ready shutdown");
        }

        // To avoid taking up too much memory, limit the queue length.
        while (queueSize.get() > this.maxBatchSize * 3) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        queue.add(data);
        queueSize.incrementAndGet();

    }


    private void startLoop() {
        new Thread(() -> {
            while (inUse.get()) {
                // Queue is empty, rest
                if (queue.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                // Batch consumption is triggered by quantity and time conditions
                long delay = System.currentTimeMillis() - lastConsumeTime;
                if (queue.size() >= this.maxBatchSize || delay >= this.maxDelayMs) {
                    dumpToConsume();
                }
            }
            // Avoid the data that is not consumed in the extreme case, and supplement it once
            dumpToConsume();
        }).start();
    }

    private void dumpToConsume() {
        draining = true;


        lastConsumeTime = System.currentTimeMillis();
        // 这里元素一般都比较多，一定要指定初始化大小。
        int size = Math.min(queueSize.get(), this.maxBatchSize);
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T item = queue.poll();

            if (item == null) {
                break;
            }

            list.add(item);
        }

        try {
            if (!list.isEmpty()) {
                consumer.accept(list);
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        } finally {
            draining = false;
            queueSize.set(queue.size());
        }
    }

    /**
     * Wait for the queue to consume
     */
    public void waitForClean() {
        LOG.info("begin wait for queue clean");
        while (draining || queue.size() > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Block the thread, wait until the data consumption is completed, and destroy the relevant resources.
     */
    public void waitForFinishAndClose() {
        waitForClean();
        inUse.set(false);
    }

    @Override
    public void close() throws Exception {
        inUse.set(false);
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getMaxBatchSize() {
        return this.maxBatchSize;
    }
}
