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

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.Job.JobMember;
import com.welab.fusion.core.Job.JobStatus;
import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.service.api.job.*;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.entity.MemberDbModel;
import com.welab.fusion.service.database.repository.JobRepository;
import com.welab.fusion.service.dto.JobConfigInput;
import com.welab.fusion.service.dto.entity.JobMemberOutputModel;
import com.welab.fusion.service.dto.entity.JobOutputModel;
import com.welab.fusion.service.job_function.MyJobFunctions;
import com.welab.fusion.service.model.FusionJobManager;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

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
    private GatewayService gatewayService;
    @Autowired
    private JobMemberService jobMemberService;

    /**
     * 创建任务
     */
    public String createJob(JobConfigInput input) throws Exception {
        checkBeforeCreateJob(input);
        // 自动保存合作方信息
        memberService.trySave(input.caller);

        JobDbModel job = new JobDbModel();
        // 来自自己前端，填充任务Id，便于其它方法统一行为。
        if (input.fromMyselfFrontEnd()) {
            input.jobId = job.getId();
            job.setCreatorMemberId(MemberService.MYSELF_NAME);
        }

        // 来自发起方，填充合作者信息。
        if (input.fromOtherFusionNode()) {
            String promoterId = MemberService.buildMemberId(input.caller.baseUrl);
            MemberDbModel promoter = memberService.findById(promoterId);

            job.setCreatorMemberId(promoterId);
            job.setPartnerMemberId(promoterId);
            job.setPartnerMemberName(promoter.getName());
        }

        job.setId(input.jobId);
        job.setRole(input.fromOtherFusionNode() ? JobMemberRole.provider : JobMemberRole.promoter);
        job.setRemark(input.remark);
        job.setStatus(JobStatus.editing);

        jobRepository.save(job);

        saveJobMember(input);
        return job.getId();
    }

    /**
     * 启动任务
     * 由协作方触发
     */
    public void startJob(JobConfigInput input) throws Exception {
        JobDbModel job = findById(input.jobId);
        if (job == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务已被删除，请重新创建。");
        }

        job.setStatus(JobStatus.running);
        if (input.fromMyselfFrontEnd()) {
            job.setRemark(input.remark);
        }
        saveJobMember(input);

        FusionNodeInfo target = memberService
                .findById(job.getPartnerMemberId())
                .toFusionNodeInfo();

        // 同步给发起方
        gatewayService.callOtherFusionNode(target, StartJobApi.class, input);


        // 创建任务并启动
        FusionJob fusionJob = createFusionJob(job);
        FusionJobManager.start(fusionJob);
    }

    private FusionJob createFusionJob(JobDbModel job) throws Exception {
        JobMemberDbModel myselfJobInfo = jobMemberService.findMyself(job.getId());
        MemberDbModel myselfInfo = memberService.findById(myselfJobInfo.getMemberId());
        DataResourceInfo myselfDataResourceInfo = DataResourceInfo.of(myselfJobInfo.getDataResourceType(), myselfJobInfo.getTotalDataCount(), myselfJobInfo.getHashConfigModel());
        JobMember myself = JobMember.of(myselfInfo.getId(), myselfInfo.getName(), myselfDataResourceInfo);

        JobMemberDbModel partnerJobInfo = jobMemberService.findByMemberId(job.getId(), job.getPartnerMemberId());
        MemberDbModel partnerInfo = memberService.findById(partnerJobInfo.getMemberId());
        DataResourceInfo partnerDataResourceInfo = DataResourceInfo.of(partnerJobInfo.getDataResourceType(), partnerJobInfo.getTotalDataCount(), partnerJobInfo.getHashConfigModel());
        JobMember partner = JobMember.of(partnerInfo.getId(), partnerInfo.getName(), partnerDataResourceInfo);

        return new FusionJob(job.getId(), myself, partner, MyJobFunctions.INSTANCE);
    }


    /**
     * 保存任务成员
     *
     * 注意：
     * updateTotalDataCount() 为异步方法
     * 不能在 JobMemberService 中调用，所以这里放在了 JobService 中。
     */
    private void saveJobMember(JobConfigInput input) throws URISyntaxException {
        jobMemberService.addMember(input);

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
    public void sendJobToProvider(SendJobApi.Input input) throws Exception {
        checkBeforeSendJob(input);

        JobDbModel job = findById(input.jobId);

        // 补充合作方信息
        String providerId = MemberService.buildMemberId(input.getBaseUrl());
        job.setPartnerMemberId(providerId);
        MemberDbModel provider = memberService.findById(providerId);
        if (provider != null) {
            job.setPartnerMemberName(provider.getName());
        }

        JobMemberDbModel promoter = jobMemberService.findMyself(job.getId());

        JobConfigInput createJobInput = new JobConfigInput();
        createJobInput.jobId = job.getId();
        createJobInput.dataResource.dataResourceType = promoter.getDataResourceType();
        createJobInput.dataResource.totalDataCount = promoter.getTotalDataCount();
        createJobInput.dataResource.hashConfig = promoter.getHashConfigModel();

        gatewayService.callOtherFusionNode(
                input.toFusionNodeInfo(),
                CreateJobApi.class,
                createJobInput
        );
    }

    /**
     * 发送任务前的检查
     */
    private void checkBeforeSendJob(SendJobApi.Input input) throws Exception {
        // 检查连通性
        memberService.testConnection(input);
    }

    /**
     * 协作方拒绝任务
     */
    public void disagree(DisagreeJobApi.Input input) throws StatusCodeWithException {
        String message = input.fromMyselfFrontEnd()
                ? "我方拒绝了任务：" + input.reason
                : "协作方拒绝了任务：" + input.reason;

        JobDbModel job = findById(input.jobId);

        if (job == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("任务已被删除，请重新创建。");
        }

        if (job.getStatus() != JobStatus.editing) {
            throw new RuntimeException("任务已经被处理，不能重复处理，请尝试新建任务。");
        }

        job.setStatus(JobStatus.disagree);
        job.setMessage(message);
        job.setUpdatedTimeNow();
        job.save();

        gatewayService.callOtherFusionNode(
                memberService.getPartnerFusionNodeInfo(job.getPartnerMemberId()),
                DisagreeJobApi.class,
                input
        );
    }

    public JobOutputModel detail(DetailJobApi.Input input) {
        JobDbModel job = findById(input.id);

        JobMemberDbModel myselfJobInfo = jobMemberService.findMyself(job.getId());
        MemberDbModel myselfInfo = memberService.findById(myselfJobInfo.getMemberId());
        JobMemberOutputModel.of(myselfInfo, myselfJobInfo);


        JobMemberDbModel partnerJobInfo = jobMemberService.findByMemberId(job.getId(), job.getPartnerMemberId());
        MemberDbModel partnerInfo = memberService.findById(job.getPartnerMemberId());

        return JobOutputModel.of(
                job,
                JobMemberOutputModel.of(myselfInfo, myselfJobInfo),
                JobMemberOutputModel.of(partnerInfo, partnerJobInfo)
        );
    }
}
