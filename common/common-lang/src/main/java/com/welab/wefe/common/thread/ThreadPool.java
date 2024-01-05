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
package com.welab.wefe.common.thread;

import cn.hutool.core.thread.NamedThreadFactory;

import java.util.concurrent.*;

/**
 * @author zane.luo
 * @date 2023/5/17
 */
public class ThreadPool {
    private ThreadPoolExecutor threadPoolExecutor;

    public ThreadPool(String threadNamePrefix) {
        this(threadNamePrefix, Runtime.getRuntime().availableProcessors());
    }

    /**
     * @param poolSize 最大并发量
     */
    public ThreadPool(String threadNamePrefix, int poolSize) {
        poolSize = Math.max(poolSize, 1);
        ThreadFactory threadFactory = new NamedThreadFactory(threadNamePrefix, false);
        this.threadPoolExecutor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                100L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory
        );
    }

    public void execute(Runnable someThing) {
        threadPoolExecutor.execute(someThing);
    }

    public void stop() {
        threadPoolExecutor.shutdownNow();
    }


    public <T> Future<T> submit(Callable<T> someThing) {
        return threadPoolExecutor.submit(someThing);
    }

    /**
     * Add an asynchronous task, and the accounting amount will be reduced after the task is executed
     */
    public void execute(Runnable someThing, CountDownLatch countDownLatch) {
        threadPoolExecutor.execute(() -> {
            try {
                someThing.run();
            } finally {
                countDownLatch.countDown();
            }
        });
    }

    public int actionThreadCount() {
        return threadPoolExecutor.getActiveCount();
    }

    public int size() {
        return threadPoolExecutor.getQueue().size();
    }

    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    public void shutdownNow() {
        threadPoolExecutor.shutdownNow();
    }

    public BlockingQueue<Runnable> getQueue() {
        return threadPoolExecutor.getQueue();
    }

    public boolean isWorking() {
        return !threadPoolExecutor.getQueue().isEmpty() || threadPoolExecutor.getActiveCount() > 0;
    }
}
