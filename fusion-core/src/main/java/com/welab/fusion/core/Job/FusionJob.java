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

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.welab.fusion.core.Job.action.AbstractJobPhaseAction;
import com.welab.fusion.core.Job.action.JobPhaseActionCreator;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.function.JobFunctions;
import com.welab.wefe.common.util.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class FusionJob implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private String jobId;
    private JobMember myself;
    private JobMember partner;
    private JobProgress myProgress = new JobProgress();
    private FusionJobRole jobRole;
    private ThreadPoolExecutor actionSingleThreadExecutor;
    private ThreadPoolExecutor scheduleSingleThreadExecutor;
    private JobFunctions jobFunctions;
    private FusionResult fusionResult;
    /**
     * 已执行的阶段
     */
    private final Set<JobPhase> executedPhases = new HashSet<>();

    public FusionJob(String jobId, JobMember myself, JobMember partner, JobFunctions jobFunctions) {
        jobFunctions.check();

        this.jobId = StrUtil.isEmpty(jobId) ? UUID.randomUUID().toString().replace("-", "") : jobId;
        this.myself = myself;
        this.partner = partner;
        this.jobFunctions = jobFunctions;

        this.fusionResult = FusionResult.of(jobId);

        this.actionSingleThreadExecutor = createSingleThreadPoll("job-" + jobId + "-action-executor");
        this.scheduleSingleThreadExecutor = createSingleThreadPoll("job-" + jobId + "-schedule");
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
        scheduleSingleThreadExecutor.submit(() -> {
            try {
                // 开始执行第一阶段任务
                gotoAction(JobPhase.firstPhase());

                while (true) {
                    if (myProgress.getJobStatus().isFinished()) {
                        break;
                    }

                    // 执行调度
                    schedule();

                    // 这里二次判断是为了快速跳出，少等待一次。
                    if (myProgress.getJobStatus().isFinished()) {
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
     */
    private void schedule() throws Exception {
        JobProgress partnerProgress = getPartnerProgress();
        // 失联，任务中止。
        if (partnerProgress == null) {
            finishJobByDisconnection();
            return;
        }

        // 还没有任何进度，跳过。
        if (partnerProgress.isEmpty()) {
            return;
        }

        // 如果对方已失败，则我方跟随失败。
        if (partnerProgress.getJobStatus().isFailed()) {
            finishJobFollowFailed(partnerProgress);
            return;
        }

        // 双方都已完成，任务结束。
        if (myProgress.getCurrentPhase().isLastPhase() && partnerProgress.getCurrentPhase().isLastPhase()) {
            if (myProgress.getJobStatus().isSuccess() && partnerProgress.getJobStatus().isSuccess()) {
                finishJobBySuccess();
                return;
            }
        }

        // 进入下一个阶段。
        if (needGotoNextPhase(partnerProgress)) {
            gotoAction(myProgress.getCurrentPhase().next());
        }
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
        if (myProgress.getCurrentPhase().isLastPhase()) {
            return false;
        }

        // 如果我方进度落后于对方，进入下一阶段。
        if (myProgress.getCurrentPhase().index() < partnerProgress.getCurrentPhase().index()) {
            return true;
        }

        // 双方进度相同，且双方都已完成，进入下一阶段。
        if (myProgress.getCurrentPhase().index() == partnerProgress.getCurrentPhase().index()) {
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

    private void finishJobBySuccess() {
        String message = "success";
        finishJob(JobStatus.success, message);
    }

    /**
     * 结束任务
     */
    private void finishJob(JobStatus status, String message) {
        LOG.info("任务结束，状态：{}，消息：{}", status, message);
        myProgress.finish(status, message);
    }

    /**
     * 获取合作伙伴的任务进度
     */
    private JobProgress getPartnerProgress() throws Exception {
        return jobFunctions.getPartnerProgressFunction.get();
    }

    /**
     * 由调度器调用，进入指定阶段。
     */
    private synchronized void gotoAction(JobPhase phase) {
        if (executedPhases.contains(phase)) {
            throw new RuntimeException("阶段已执行过，不能重复执行。");
        }
        executedPhases.add(phase);

        // 异步执行当前阶段动作
        AbstractJobPhaseAction action = JobPhaseActionCreator.create(phase, this);
        actionSingleThreadExecutor.submit(() -> {
            action.run();
        });
    }

    private void checkBeforeFusion() throws Exception {
        if (myself.dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter && partner.dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {
            throw new Exception("不能双方都使用布隆过滤器，建议数据量大的一方使用布隆过滤器。");
        }
    }

    private ThreadPoolExecutor createSingleThreadPoll(String prefix) {
        ThreadFactory threadFactory = new NamedThreadFactory(prefix, false);
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory
        );
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
    public void close() throws IOException {
        scheduleSingleThreadExecutor.shutdownNow();
        actionSingleThreadExecutor.shutdownNow();

        if (myself.tableDataResourceReader != null) {
            myself.tableDataResourceReader.close();
        }
    }

    // region getter/setter

    public JobMember getMyself() {
        return myself;
    }

    public JobMember getPartner() {
        return partner;
    }

    public void setMyRole(FusionJobRole jobRole) {
        this.jobRole = jobRole;
    }

    public FusionJobRole getMyJobRole() {
        return jobRole;
    }

    public JobProgress getMyProgress() {
        return myProgress;
    }

    public String getJobId() {
        return jobId;
    }

    public JobFunctions getJobFunctions() {
        return jobFunctions;
    }

    public FusionResult getJobResult() {
        return fusionResult;
    }

    // endregion

}
