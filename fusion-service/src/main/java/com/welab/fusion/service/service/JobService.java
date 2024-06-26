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
package com.welab.fusion.service.service;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.Job.PsiJobResult;
import com.welab.fusion.core.Job.base.JobStatus;
import com.welab.fusion.core.progress.JobProgress;
import com.welab.fusion.service.api.job.*;
import com.welab.fusion.service.api.job.schedule.AgreeAndStartJobApi;
import com.welab.fusion.service.api.job.schedule.RestartJobApi;
import com.welab.fusion.service.api.job.schedule.SendJobToProviderApi;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.base.MySpecification;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.entity.MemberDbModel;
import com.welab.fusion.service.database.repository.JobRepository;
import com.welab.fusion.service.dto.JobConfigInput;
import com.welab.fusion.service.dto.JobMemberDataResourceInput;
import com.welab.fusion.service.dto.base.PagingOutput;
import com.welab.fusion.service.dto.entity.JobMemberOutputModel;
import com.welab.fusion.service.dto.entity.JobOutputModel;
import com.welab.fusion.service.model.FusionJobManager;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Service
public class JobService extends AbstractService {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private JobMemberService jobMemberService;
    @Autowired
    private CreatePsiJobService createPsiJobService;

    /**
     * 关闭之前处于运行中的任务
     */
    @Async
    public void finishAllJob() {
        List<JobDbModel> jobs = jobRepository.findAllRunningJob();
        for (JobDbModel job : jobs) {
            job.setEndTime(null);
            job.setCostTime(null);
            job.setStatus(JobStatus.error_on_running);
            job.setMessage("因服务重启导致任务中断，进度已丢失。");
            job.setProgressDetail(null);
            job.setUpdatedTimeNow();

            jobRepository.save(job);
        }
    }

    public synchronized void finish(String jobId, JobProgress progress) {
        JobDbModel job = findById(jobId);

        if (job.getStatus().isFinished()) {
            return;
        }

        job.setEndTime(new Date());
        job.setCostTime(job.getEndTime().getTime() - job.getStartTime().getTime());
        job.setStatus(progress.getJobStatus());
        job.setMessage(progress.getMessage());
        job.setProgressDetail(progress.toJson());
        job.setUpdatedTimeNow();

        jobRepository.save(job);

        FusionJobManager.remove(jobId);
    }

    /**
     * 创建任务
     */
    public String createJob(JobConfigInput input) throws Exception {
        checkBeforeCreateJob(input);
        // 自动保存合作方信息
        memberService.trySave(input.caller);
        // 自动保存数据源信息
        dataSourceService.trySave(input);

        JobDbModel job = new JobDbModel();
        job.setAlgorithm(input.algorithm);
        // 来自自己前端，填充任务Id，便于其它方法统一行为。
        if (input.isRequestFromMyself()) {
            input.jobId = job.getId();
            job.setCreatorMemberId(MemberService.MYSELF_NAME);
        }

        // 来自发起方，填充合作者信息。
        if (input.isRequestFromPartner()) {
            if (findById(input.jobId) != null) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("任务已存在，请勿重复创建。");
            }
            String promoterId = MemberService.buildMemberId(input.caller.baseUrl);
            MemberDbModel promoter = memberService.findById(promoterId);

            job.setCreatorMemberId(promoterId);
            job.setPartnerMemberId(promoterId);
            job.setPartnerMemberName(promoter.getName());
        }

        job.setId(input.jobId);
        job.setRole(input.isRequestFromPartner() ? JobMemberRole.provider : JobMemberRole.promoter);
        job.setRemark(input.remark);
        job.setStatus(input.isRequestFromMyself() ? JobStatus.editing : JobStatus.auditing);

        jobRepository.save(job);

        saveJobMember(JobMemberRole.promoter, input);

        ListAuditingJobApi.cleanCache();

        return job.getId();
    }

    /**
     * 对曾经执行过的任务进行重启
     */
    public void restart(RestartJobApi.Input input) throws Exception {
        JobDbModel job = findById(input.jobId);
        if (job == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务已被删除，请重新创建。");
        }

        if (!job.getStatus().isFinished()) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("仅支持对执行过的任务进行重启");
        }

        job.setStatus(JobStatus.running);
        job.setStartTime(new Date());
        if (input.isRequestFromMyself() && StringUtil.isNotEmpty(input.remark)) {
            job.setRemark(input.remark);
        }
        job.setProgressDetail(new JobProgress().toJson());
        jobRepository.save(job);

        FusionJobManager.remove(input.jobId);

        FusionNodeInfo target = memberService
                .findById(job.getPartnerMemberId())
                .toFusionNodeInfo();

        try {
            // 同步给发起方
            gatewayService.callOtherFusionNode(target, RestartJobApi.class, input);

            // 创建任务并启动
            AbstractPsiJob psiJob = createPsiJobService.createPsiJob(job);
            FusionJobManager.start(psiJob);
        } catch (Exception e) {
            job.setStatus(JobStatus.error_on_running);
            job.setMessage("任务重启失败：" + e.getMessage());
            job.setEndTime(new Date());
            job.setCostTime(System.currentTimeMillis() - job.getStartTime().getTime());
            jobRepository.save(job);
            throw e;
        } finally {
            ListAuditingJobApi.cleanCache();
        }

    }

    /**
     * 启动任务
     * 由协作方触发
     */
    public void agreeAndStartJob(JobConfigInput input) throws Exception {
        // 自动保存数据源信息
        dataSourceService.trySave(input);

        JobDbModel job = findById(input.jobId);
        if (job == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务已被删除，请重新创建。");
        }

        job.setStatus(JobStatus.running);
        job.setStartTime(new Date());
        if (input.isRequestFromMyself()) {
            job.setRemark(input.remark);
        }

        saveJobMember(JobMemberRole.provider, input);

        FusionNodeInfo target = memberService
                .findById(job.getPartnerMemberId())
                .toFusionNodeInfo();


        try {
            // 同步给发起方
            gatewayService.callOtherFusionNode(target, AgreeAndStartJobApi.class, input);

            // 创建任务并启动
            AbstractPsiJob psiJob = createPsiJobService.createPsiJob(job);
            FusionJobManager.start(psiJob);
        } catch (Exception e) {
            job.setStatus(JobStatus.error_on_running);
            job.setMessage("任务启动失败：" + e.getMessage());
            job.setEndTime(new Date());
            job.setCostTime(System.currentTimeMillis() - job.getStartTime().getTime());
            jobRepository.save(job);
            throw e;
        } finally {
            ListAuditingJobApi.cleanCache();
        }
    }


    /**
     * 保存任务成员
     *
     * 注意：
     * updateTotalDataCount() 为异步方法
     * 不能在 JobMemberService 中调用，所以这里放在了 JobService 中。
     */
    private void saveJobMember(JobMemberRole role, JobConfigInput input) throws URISyntaxException {
        jobMemberService.putMember(role, input);

        // 更新我方资源数据量
        jobMemberService.updateTotalDataCount(input);
    }

    private void checkBeforeCreateJob(JobConfigInput input) {

    }

    public JobDbModel findById(String jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }

    /**
     * 发起方将任务发送到协作方
     */
    public void sendJobToProvider(SendJobToProviderApi.Input input) throws Exception {
        checkBeforeSendJob(input);
        // 保存合作方信息
        jobMemberService.addProvider(input);

        JobDbModel job = findById(input.jobId);

        // 补充合作方信息
        String providerId = MemberService.buildMemberId(input.getBaseUrl());
        job.setPartnerMemberId(providerId);
        MemberDbModel provider = memberService.findById(providerId);
        if (provider != null) {
            job.setPartnerMemberName(provider.getName());
        }
        job.setStatus(JobStatus.auditing);
        jobRepository.save(job);

        // 发送任务到协作方
        JobMemberDbModel promoter = jobMemberService.findMyself(job.getId());

        JobMemberDataResourceInput dataResource = new JobMemberDataResourceInput();
        dataResource.dataResourceType = promoter.getDataResourceType();
        dataResource.totalDataCount = promoter.getTotalDataCount();
        dataResource.hashConfig = promoter.getHashConfigModel();
        dataResource.additionalResultColumns = promoter.getAdditionalResultColumns();

        JobConfigInput createJobInput = new JobConfigInput();
        createJobInput.jobId = job.getId();
        createJobInput.dataResource = dataResource;
        createJobInput.algorithm = job.getAlgorithm();

        gatewayService.callOtherFusionNode(
                input.toFusionNodeInfo(),
                CreateJobApi.class,
                createJobInput
        );
    }

    /**
     * 发送任务前的检查
     */
    private void checkBeforeSendJob(SendJobToProviderApi.Input input) throws Exception {
        // 检查连通性
        memberService.testConnection(input);
    }

    /**
     * 协作方拒绝任务
     */
    public void disagree(DisagreeJobApi.Input input) throws StatusCodeWithException {
        String message = input.isRequestFromMyself()
                ? "我方拒绝了任务：" + input.reason
                : "协作方拒绝了任务：" + input.reason;

        JobDbModel job = findById(input.jobId);

        if (job == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务已被删除，请重新创建。");
        }

        if (job.getStatus() != JobStatus.editing && job.getStatus() != JobStatus.auditing) {
            throw new RuntimeException("任务已经被处理，不能重复处理，请尝试新建任务。");
        }

        job.setStatus(JobStatus.disagree);
        job.setMessage(message);
        job.setUpdatedTimeNow();
        jobRepository.save(job);

        ListAuditingJobApi.cleanCache();
        
        gatewayService.callOtherFusionNode(
                memberService.getPartnerFusionNodeInfo(job.getPartnerMemberId()),
                DisagreeJobApi.class,
                input
        );
    }

    public JobOutputModel detail(DetailJobApi.Input input) throws StatusCodeWithException {
        JobDbModel job = findById(input.id);
        if (job == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("错误的任务Id，任务不存在：" + input.id);
        }
        return jobToOutputModel(job);
    }

    private JobOutputModel jobToOutputModel(JobDbModel job) {
        JobMemberDbModel myselfJobInfo = jobMemberService.findMyself(job.getId());
        MemberDbModel myselfInfo = null;
        try {
            myselfInfo = memberService.getMyself();
        } catch (Exception e) {
            // ignore
        }

        JobMemberDbModel partnerJobInfo = null;
        MemberDbModel partnerInfo = null;
        if (StringUtil.isNotEmpty(job.getPartnerMemberId())) {
            partnerJobInfo = jobMemberService.findByMemberId(job.getId(), job.getPartnerMemberId());
            partnerInfo = memberService.findById(job.getPartnerMemberId());
        }

        return JobOutputModel.of(
                job,
                JobMemberOutputModel.of(myselfInfo, myselfJobInfo),
                JobMemberOutputModel.of(partnerInfo, partnerJobInfo)
        );
    }

    public PagingOutput<JobOutputModel> query(QueryJobApi.Input input) {
        MySpecification<JobDbModel> where = Where.create()
                .equal("id", input.jobId)
                .equal("status", input.status)
                .equal("role", input.role)
                .build();
        PagingOutput<JobDbModel> paging = jobRepository.paging(where, input);

        // 转换类型
        List<JobOutputModel> list = paging.getList()
                .stream()
                .map(this::jobToOutputModel)
                .collect(Collectors.toList());

        return PagingOutput.of(paging.getTotal(), list);
    }

    /**
     * 删除任务
     * 1. 删除数据库记录
     * 2. 删除求交结果
     */
    public void delete(String id) throws StatusCodeWithException {
        JobDbModel job = findById(id);
        if (job == null) {
            return;
        }

        if (job.getStatus().isRunning()) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务正在运行中，不能删除。");
        }

        // 删除求交结果
        File file = job.getResultFile();
        if (file != null && file.exists()) {
            file.delete();
        }

        // 应删尽删
        FusionJobManager.remove(id);
        jobMemberService.deleteByJobId(id);
        jobRepository.deleteById(id);

        ListAuditingJobApi.cleanCache();
    }

    /**
     * 获取我方任务进度
     */
    public JobProgress getMyJobProgress(String jobId) {
        AbstractPsiJob job = FusionJobManager.get(jobId);
        if (job != null) {
            return job.getMyProgress();
        }

        return findById(jobId).getProgressModel();
    }

    /**
     * 不论己方为何角色，返回合作方信息。
     */
    public MemberDbModel findPartner(String jobId) {
        JobDbModel job = findById(jobId);
        return memberService.findById(job.getPartnerMemberId());
    }

    /**
     * 手动停止任务
     */
    public void stop(String id) throws StatusCodeWithException {
        JobDbModel job = findById(id);
        if (job == null) {
            return;
        }

        if (!job.getStatus().isRunning()) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务并未运行，无法执行中止动作。");
        }

        FusionJobManager.get(id).finishJobByUserStop();
    }

    public void savePsiResult(String jobId, PsiJobResult result) {
        JobDbModel job = findById(jobId);

        job.setFusionCount(result.fusionCount);
        job.setUpdatedTimeNow();

        jobRepository.save(job);
    }

    public void updateProgress(String jobId, JobProgress progress) {
        JobDbModel job = findById(jobId);

        if (job.getStatus().isFinished()) {
            return;
        }

        job.setStatus(progress.getJobStatus());
        job.setMessage(progress.getMessage());
        job.setProgressDetail(progress.toJson());
        job.setUpdatedTimeNow();

        jobRepository.save(job);
    }

    public void finishOnPartnerFinished(String jobId) {
        JobDbModel job = findById(jobId);

        if (job.getStatus().isFinished()) {
            return;
        }
        JobProgress progress = job.getProgressModel();
        progress.finish(JobStatus.error_on_running, "合作方已停止，我方跟随其结束任务。");

        finish(jobId, progress);
    }
}
