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
package com.welab.fusion.core.Job;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.welab.fusion.core.Job.base.*;
import com.welab.fusion.core.algorithm.base.AbstractJobFlow;
import com.welab.fusion.core.algorithm.base.PsiAlgorithm;
import com.welab.fusion.core.algorithm.base.phase_action.AbstractJobPhaseAction;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.progress.JobProgress;
import com.welab.wefe.common.thread.ThreadPool;
import com.welab.wefe.common.util.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public abstract class AbstractPsiJob implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private String jobId;
    private JobTempData jobTempData = new JobTempData();
    private AbstractJobMember myself;
    private AbstractJobMember partner;
    private JobProgress myProgress = new JobProgress();
    private JobRole jobRole;
    private ThreadPool actionSingleThreadExecutor;
    private ThreadPool scheduleSingleThreadExecutor;
    private AbstractJobFunctions jobFunctions;
    private PsiJobResult psiJobResult;
    private PsiAlgorithm algorithm;
    private AbstractJobFlow jobFlow;
    private boolean jobClosed = false;
    /**
     * 双方进度不一致时，记录时间。
     */
    private long firstJobPhaseNotEqualTime;
    /**
     * 已执行的阶段
     */
    private final Set<JobPhase> executedPhases = new HashSet<>();

    public AbstractPsiJob(PsiAlgorithm algorithm, String jobId, AbstractJobMember myself, AbstractJobMember partner, AbstractJobFunctions jobFunctions) {
        jobFunctions.check();

        this.algorithm = algorithm;
        this.jobFlow = algorithm.createJobFlow();
        this.jobId = StrUtil.isEmpty(jobId) ? UUID.randomUUID().toString().replace("-", "") : jobId;
        this.myself = myself;
        this.partner = partner;
        this.jobFunctions = jobFunctions;

        this.psiJobResult = PsiJobResult.of(jobId);

        this.actionSingleThreadExecutor = new ThreadPool("job-" + jobId + "-action-executor", 1);
        this.scheduleSingleThreadExecutor = new ThreadPool("job-" + jobId + "-schedule", 1);
    }

    /**
     * 此方法会执行一些事前检查后进入异步执行
     * 使用异步线程调度任务，使其按顺序执行各阶段动作，并在必要时结束任务。
     */
    public void start() throws Exception {
        checkBeforeFusion();
        startSchedule();
    }

    /**
     * 启动调度线程
     * 调度线程负责调度
     *
     * 这里采取观察者模式，各方根据观察到的信息调度自己的任务。
     * 不对其它方的任务进行主动通知。
     */
    private void startSchedule() {
        LOG.info("启动监工线程，开始任务。");
        firstJobPhaseNotEqualTime = System.currentTimeMillis();
        scheduleSingleThreadExecutor.execute(() -> {
            try {
                // 开始执行第一阶段任务
                gotoPhase(jobFlow.firstPhase());

                while (true) {

                    // 执行调度
                    boolean keep = schedule();
                    if (!keep) {
                        break;
                    }

                    if (jobClosed) {
                        break;
                    }

                    // 考虑到要获取合作方的进度需要远程调用，这里不要太频繁。
                    ThreadUtil.safeSleep(5_000);
                }
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                finishJobOnException(e);
            } finally {
                CloseableUtils.closeQuietly(this);
            }
        });
        scheduleSingleThreadExecutor.shutdown();
    }

    /**
     * 观察，并调度任务。
     *
     * @return 是否继续调度
     */
    private boolean schedule() throws Exception {
        JobProgress partnerProgress = getPartnerProgress();
        // 失联，任务中止。
        if (partnerProgress == null) {
            finishJobByDisconnection();
            return false;
        }

        // 当双方进度不一致时，记录时间。
        if (myProgress.getCurrentPhase() == partnerProgress.getCurrentPhase()) {
            firstJobPhaseNotEqualTime = System.currentTimeMillis();
        }
        // 如果双方进度偏离时间过长，任务中止。
        if (System.currentTimeMillis() - firstJobPhaseNotEqualTime > 1000 * 60) {
            finishJobByDisconnection();
            return false;
        }

        // 还没有任何进度，等待。
        if (partnerProgress.isEmpty()) {
            return true;
        }

        // 如果对方已失败，则我方跟随失败。
        if (partnerProgress.getJobStatus().isFailed()) {
            finishJobFollowFailed(partnerProgress);
            return false;
        }

        // 双方都已完成，任务结束。
        boolean allAreLastPhase = jobFlow.isLastPhase(myProgress.getCurrentPhase()) && jobFlow.isLastPhase(partnerProgress.getCurrentPhase());
        boolean allAreSuccess = myProgress.getCurrentPhaseStatus().isSuccess() && partnerProgress.getCurrentPhaseStatus().isSuccess();
        if (allAreLastPhase && allAreSuccess) {
            finishJobBySuccess();
            return false;
        }

        // 进入下一个阶段。
        if (needGotoNextPhase(partnerProgress)) {
            gotoPhase(jobFlow.nextPhase(myProgress.getCurrentPhase()));
        }

        return true;
    }


    /**
     * 是否需要进入下一阶段
     */
    private boolean needGotoNextPhase(JobProgress partnerProgress) {

        // 我方未完成，不进入下一阶段。
        if (!myProgress.getCurrentPhaseStatus().isSuccess()) {
            return false;
        }

        // 已经是最后一个阶段，不进入下一阶段。
        if (jobFlow.isLastPhase(myProgress.getCurrentPhase())) {
            return false;
        }

        // 如果我方进度落后于对方，进入下一阶段。
        if (jobFlow.phaseIndex(myProgress.getCurrentPhase()) < jobFlow.phaseIndex(partnerProgress.getCurrentPhase())) {
            return true;
        }

        // 双方进度相同，且双方都已完成，进入下一阶段。
        if (jobFlow.phaseIndex(myProgress.getCurrentPhase()) == jobFlow.phaseIndex(partnerProgress.getCurrentPhase())) {
            if (myProgress.getCurrentPhaseStatus().isSuccess() && partnerProgress.getCurrentPhaseStatus().isSuccess()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 因与合作方断线失联引起的任务中止
     */
    private void finishJobByDisconnection() {
        String message = "与成员[" + partner.memberName + "]失联，我方停止任务。";
        finishJob(JobStatus.error_on_running, message);
    }

    /**
     * 跟随合作伙伴失败
     */
    private void finishJobFollowFailed(JobProgress partnerProgress) {
        String message = "成员[" + partner.memberName + "]已中止任务，我方跟随其中止。";
        finishJob(JobStatus.error_on_running, message);
    }

    /**
     * 我方任务异常中止
     */
    public void finishJobOnException(Exception e) {
        String message = "成员[" + myself.memberName + "]发生异常：" + e.getMessage();
        finishJob(JobStatus.error_on_running, message);
    }

    /**
     * 手动中止
     */
    public void finishJobByUserStop() {
        String message = "成员[" + myself.memberName + "]停止了任务。";
        finishJob(JobStatus.stop_on_running, message);
    }

    private void finishJobBySuccess() {
        String message = "success";
        finishJob(JobStatus.success, message);
    }

    /**
     * 结束任务
     */
    private synchronized void finishJob(JobStatus status, String message) {

        LOG.info("任务结束，状态：{}，消息：{}", status, message);
        myProgress.finish(status, message);
        getJobResult().finish();

        try {
            jobFunctions.finishJobFunction.finish(jobId, myProgress);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }

        CloseableUtils.closeQuietly(this);
    }

    /**
     * 获取合作伙伴的任务进度
     */
    private JobProgress getPartnerProgress() throws Exception {
        return jobFunctions.getPartnerProgressFunction.get(jobId);
    }

    /**
     * 由调度器调用，进入指定阶段。
     */
    private synchronized void gotoPhase(JobPhase phase) {
        if (executedPhases.contains(phase)) {
            throw new RuntimeException("阶段已执行过，不能重复执行：" + phase);
        }
        executedPhases.add(phase);

        // 异步执行当前阶段动作
        AbstractJobPhaseAction action = jobFlow.createAction(phase, this);
        actionSingleThreadExecutor.execute(() -> {
            action.run();
        });
    }

    /**
     * 等待任务结束
     */
    public void waitFinish() {
        while (!isFinished()) {
            ThreadUtil.safeSleep(1000);
        }
    }

    /**
     * 任务是否已结束
     */
    public boolean isFinished() {
        if (myProgress.isEmpty()) {
            return false;
        }

        return myProgress.getJobStatus().isFinished();
    }

    @Override
    public synchronized void close() throws IOException {
        if (jobClosed) {
            return;
        }
        jobClosed = true;

        scheduleSingleThreadExecutor.shutdownNow();
        actionSingleThreadExecutor.shutdownNow();

        CloseableUtils.closeQuietly(myself);
        CloseableUtils.closeQuietly(partner);

        try {
            FileSystem.JobTemp.clean(jobId);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
    }

    // region getter/setter


    public PsiAlgorithm getAlgorithm() {
        return algorithm;
    }

    public JobTempData getJobTempData() {
        return jobTempData;
    }

    public void setMyRole(JobRole jobRole) {
        this.jobRole = jobRole;
    }

    public JobRole getMyJobRole() {
        return jobRole;
    }

    public JobProgress getMyProgress() {
        return myProgress;
    }

    public String getJobId() {
        return jobId;
    }

    public PsiJobResult getJobResult() {
        return psiJobResult;
    }

    // endregion


    // region abstract
    protected abstract void checkBeforeFusion() throws Exception;

    public abstract <T extends AbstractJobMember> T getMyself();

    public abstract <T extends AbstractJobMember> T getPartner();

    public abstract <T extends AbstractJobFunctions> T getJobFunctions();

    // endregion

}
