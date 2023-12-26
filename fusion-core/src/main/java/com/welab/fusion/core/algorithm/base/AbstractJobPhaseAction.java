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
package com.welab.fusion.core.algorithm.base;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.Job.JobStatus;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.progress.JobPhaseProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public abstract class AbstractJobPhaseAction<T extends AbstractPsiJob> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected T job;
    protected JobPhaseProgress phaseProgress;

    /**
     * 执行动作
     */
    protected abstract void doAction() throws Exception;

    /**
     * 获取当前阶段类型
     */
    public abstract JobPhase getPhase();

    /**
     * 获取当前阶段总工作量
     */
    public abstract long getTotalWorkload();

    public AbstractJobPhaseAction(T job) {
        this.job = job;
        this.phaseProgress = JobPhaseProgress.of(
                job.getJobId(),
                getPhase(),
                0
        );

        this.phaseProgress.setMessage("开始执行阶段动作: " + getPhase().getLabel());
    }

    /**
     * 是否跳过此阶段
     */
    protected abstract boolean skipThisAction();

    /**
     * 由于此方法运行于异步线程中，所以需要自行捕获异常。
     */
    public void run() {
        LOG.info("start phase: {}", getPhase());
        long start = System.currentTimeMillis();

        JobStatus status = JobStatus.success;
        String message = "success";

        try {
            boolean skipThisAction = skipThisAction();

            // 初始化当前阶段进度
            if (!skipThisAction) {
                phaseProgress.updateCompletedWorkload(getTotalWorkload());
            }

            // 添加当前阶段进度到总进度
            job.getMyProgress().addPhaseProgress(phaseProgress);

            if (skipThisAction) {
                phaseProgress.skipThisPhase();
            } else {
                doAction();
            }

        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            status = JobStatus.error_on_running;
            message = e.getMessage();
            job.finishJobOnException(e);
        } finally {
            phaseProgress.finish(status, message);
            long spend = System.currentTimeMillis() - start;
            LOG.info("finished phase: {} spend: {}ms", getPhase(), spend);
        }

    }

    /**
     * 从合作方下载文件
     */
    public File downloadFileFromPartner() throws Exception {
        return job.getJobFunctions().downloadPartnerFileFunction.download(
                getPhase(),
                job.getJobId(),
                job.getPartner().memberId,
                size -> {
                    phaseProgress.updateTotalWorkload(size);
                },
                size -> {
                    phaseProgress.updateCompletedWorkload(size);
                }
        );
    }
}
