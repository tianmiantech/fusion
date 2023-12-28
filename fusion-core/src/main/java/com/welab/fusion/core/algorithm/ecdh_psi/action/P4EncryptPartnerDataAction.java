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
package com.welab.fusion.core.algorithm.ecdh_psi.action;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadUtil;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author zane.luo
 * @date 2023/12/20
 */
public class P4EncryptPartnerDataAction extends AbstractJobPhaseAction<EcdhPsiJob> {
    private final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() - 2,
            Runtime.getRuntime().availableProcessors() - 2,
            10L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new NamedThreadFactory("encrypt_partner_data-thread-pool-", true)
    );
    /**
     * 保存加密后的数据的文件
     * 务必使用线程安全的写入方式
     */
    private BufferedWriter fileWriter;
    private LongAdder progress = new LongAdder();

    public P4EncryptPartnerDataAction(EcdhPsiJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        this.fileWriter = initFile();

        // 读取合作方的加密数据，使用我方秘钥对其二次加密。
        File partnerData = job.getPartner().psiECEncryptedData.getDataFile();
        try (CsvTableDataSourceReader reader = new CsvTableDataSourceReader(partnerData)) {
            reader.readRows((index, row) -> {
                encryptOne(row);
            });
        }

        while (isWorking()) {
            ThreadUtil.safeSleep(1000);
        }

        this.fileWriter.close();
    }

    /**
     * 对一行数据进行二次加密，并将加密结果写入文件。
     */
    private void encryptOne(LinkedHashMap<String, Object> row) {

        // 避免读取的数据堆积在内存
        while (THREAD_POOL.getQueue().size() > 1000) {
            ThreadUtil.safeSleep(100);
        }

        THREAD_POOL.execute(() -> {
            String key = row.get(Constant.KEY_COLUMN_NAME).toString();
            String encrypted = job.getMyself().psiECEncryptedData.encryptPartnerData(key);
            try {
                String line = row.get(Constant.INDEX_COLUMN_NAME) + ","
                        + encrypted
                        + System.lineSeparator();

                this.fileWriter.append(line);
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }

            progress.increment();
            phaseProgress.updateCompletedWorkload(progress.longValue());
        });
    }

    private BufferedWriter initFile() throws IOException {
        // 初始化输出文件
        File outputFile = FileSystem
                .PsiSecondaryECEncryptedData
                .getDataFile(job.getJobId());

        outputFile.delete();
        outputFile.getParentFile().mkdirs();
        job.getPartner().secondaryECEncryptedDataFile = outputFile;

        BufferedWriter writer = FileUtil.buildBufferedWriter(outputFile, false);

        // 写入列头
        writer.write(
                Constant.INDEX_COLUMN_NAME + ","
                        + Constant.KEY_COLUMN_NAME
                        + System.lineSeparator()
        );

        return writer;
    }

    private boolean isWorking() {
        return !THREAD_POOL.getQueue().isEmpty() || THREAD_POOL.getActiveCount() > 0;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.ECEncryptPartnerData;
    }

    @Override
    public long getTotalWorkload() {
        return job.getPartner().psiECEncryptedData.insertedElementCount;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }

}
