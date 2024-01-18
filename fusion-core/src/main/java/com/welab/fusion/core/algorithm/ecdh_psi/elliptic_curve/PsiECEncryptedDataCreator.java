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
package com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve;

import cn.hutool.core.thread.ThreadUtil;
import com.welab.fusion.core.io.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.progress.Progress;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.thread.ThreadPool;
import com.welab.wefe.common.util.CloseableUtils;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/12/19
 */
public class PsiECEncryptedDataCreator implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private AbstractTableDataSourceReader dataSourceReader;
    private PsiECEncryptedData psiECEncryptedData;
    /**
     * 用来异步生成过滤器的线程池
     */
    private ThreadPool singleThreadExecutor;
    private Progress progress;

    public PsiECEncryptedDataCreator(String id, AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig) {
        this(
                id,
                dataSourceReader,
                hashConfig,
                Progress.of(id, dataSourceReader.getTotalDataRowCount())
        );
    }

    public PsiECEncryptedDataCreator(String id, AbstractTableDataSourceReader dataSourceReader, HashConfig hashConfig, Progress progress) {
        this.dataSourceReader = dataSourceReader;
        this.singleThreadExecutor = new ThreadPool("create-psi_elliptic_curve_encrypted_data:" + id, 1);

        this.progress = progress;

        this.psiECEncryptedData = PsiECEncryptedData.of(
                id,
                hashConfig,
                EcdhPsiParam.of(generateSecretKey(EllipticCurve.EC_PARAMETER_SPEC))
        );
    }

    /**
     * 随机生成私钥
     */
    private static BigInteger generateSecretKey(ECParameterSpec ecSpec) {
        String str = "65178270573371609295462458881188300559064589938737636855586770160208398352236";
        if (true) {
            return new BigInteger(str, 10);
        }

        BigInteger n = ecSpec.getN();
        // 随机状态
        SecureRandom secureRandom = new SecureRandom();
        BigInteger k = new BigInteger(n.bitLength(), secureRandom).mod(n);
        return k;
    }

    public PsiECEncryptedData create() throws Exception {
        return create(null);
    }

    /**
     * 从数据源读取数据，加密，写入布隆过滤器。
     *
     * @param progressConsumer 进度回调
     */
    public PsiECEncryptedData create(Consumer<Progress> progressConsumer) throws Exception {
        LOG.info("start to create PsiEllipticCurveEncryptedData. id:{}", psiECEncryptedData.id);
        final long start = System.currentTimeMillis();

        AtomicReference<Exception> error = new AtomicReference<>();
        try (PsiECEncryptedDataConsumer consumer = new PsiECEncryptedDataConsumer(psiECEncryptedData, progress)) {
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

            if (consumer.getError() != null) {
                throw consumer.getError();
            }

            psiECEncryptedData.insertedElementCount = consumer.getInsertedElementCount().longValue();
        }

        long spend = System.currentTimeMillis() - start;
        LOG.info("create PsiBloomFilter success({}). id:{}", TimeSpan.fromMs(spend), psiECEncryptedData.id);

        return psiECEncryptedData;
    }

    public Progress getProgress() {
        return progress;
    }

    @Override
    public void close() throws IOException {
        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdownNow();
        }

        CloseableUtils.closeQuietly(dataSourceReader);
    }


    /**
     * 测试
     */
    public static void main(String[] args) throws Exception {
        FileSystem.init("D:\\data\\fusion");
        File file = new File("D:\\data\\wefe\\ivenn_10w_20210319_vert_promoter.csv");
        // File file = new File("D:\\data\\wefe\\3x100000000rows-miss0-auto_increment.csv");
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file);
        System.out.println(reader.getHeader());
        HashConfig hashConfig = HashConfig.of(HashConfigItem.of(HashMethod.MD5, "id"));

        // 生成过滤器
        try (PsiECEncryptedDataCreator creator = new PsiECEncryptedDataCreator("test", reader, hashConfig)) {
            PsiECEncryptedData data = creator.create(progress -> {
                System.out.println("进度：" + progress.getCompletedWorkload() + "," + progress.getSpeedInSecond() + "条/秒");
            });

            data.sink();

        }

        System.out.println("end");
    }
}
