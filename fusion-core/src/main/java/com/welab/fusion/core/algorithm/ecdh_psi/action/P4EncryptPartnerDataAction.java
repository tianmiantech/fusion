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
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.util.StringUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        // 初始化输出文件
        File outputFile = FileSystem
                .PsiSecondaryECEncryptedData
                .getDataFile(job.getJobId());
        outputFile.delete();
        outputFile.getParentFile().mkdirs();
        this.fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, false), StandardCharsets.UTF_8));

        // 读取合作方的加密数据，使用我方秘钥对其二次加密。
        File partnerData = job.getPartner().psiECEncryptedData.getDataFile();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(partnerData), StandardCharsets.UTF_8))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                encryptOne(line);
            }
        }

        while (isWorking()) {
            ThreadUtil.safeSleep(1000);
        }

        job.getPartner().secondaryECEncryptedDataFile = outputFile;

        phaseProgress.success();
    }

    private void encryptOne(String line) {
        // 丢弃空行
        if (StringUtil.isBlank(line)) {
            return;
        }

        // 避免读取的数据堆积在内存
        while (THREAD_POOL.getQueue().size() > 1000) {
            ThreadUtil.safeSleep(100);
        }

        THREAD_POOL.execute(() -> {
            String encrypted = job.getMyself().psiECEncryptedData.encryptPartnerData(line);
            // 将加密后的数据保存到文件
            try {
                // 这里写入的必须拼接上换行符之后写入，如果分开写入，在并发的影响下，会导致数据错乱。
                this.fileWriter.append(encrypted + System.lineSeparator());
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }

            progress.increment();
            phaseProgress.updateCompletedWorkload(progress.longValue());
        });
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
