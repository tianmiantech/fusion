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

import com.welab.fusion.service.api.job.CreateJobApi;
import com.welab.fusion.service.api.job.SendJobApi;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.entity.PartnerDbModel;
import com.welab.fusion.service.database.repository.JobRepository;
import com.welab.fusion.service.dto.JobConfigInput;
import com.welab.fusion.service.service.base.AbstractService;
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
    private PartnerService partnerService;
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
        partnerService.trySave(input.caller);

        JobDbModel job = new JobDbModel();
        // 来自自己前端，填充任务Id，便于其它方法统一行为。
        if (input.fromMyselfFrontEnd()) {
            input.jobId = job.getId();
            job.setPartnerId(PartnerService.MYSELF_NAME);
        }

        // 来自发起方，填充合作者信息。
        if (input.fromPartner()) {
            String promoterId = PartnerService.buildPartnerId(input.caller.baseUrl);
            PartnerDbModel promoter = partnerService.findById(promoterId);

            job.setPartnerId(promoterId);
            job.setPartnerName(promoter.getName());
        }

        job.setId(input.jobId);
        job.setRole(input.fromPartner() ? JobMemberRole.provider : JobMemberRole.promoter);
        job.setRemark(input.remark);


        jobRepository.save(job);

        saveJobMember(input);
        return job.getId();
    }

    /**
     * 启动任务
     * 由协作方触发
     */
    public void start(JobConfigInput input) throws URISyntaxException {
        JobDbModel job = findById(input.jobId);
        job.setRemark(input.remark);
        saveJobMember(input);

        FusionNodeInfo target = partnerService
                .findById(job.getPartnerId())
                .toFusionNodeInfo();

        // 同步给发起方
        gatewayService.callOtherPartner(target, CreateJobApi.class, input);
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

    public void startJob(String jobId) {
        JobDbModel job = findById(jobId);
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
        String providerId = PartnerService.buildPartnerId(input.getBaseUrl());
        job.setPartnerId(providerId);
        PartnerDbModel provider = partnerService.findById(providerId);
        if (provider != null) {
            job.setPartnerName(provider.getName());
        }

        JobMemberDbModel promoter = jobMemberService.findMyself(job.getId());

        JobConfigInput createJobInput = new JobConfigInput();
        createJobInput.jobId = job.getId();
        createJobInput.dataResource.dataResourceType = promoter.getDataResourceType();
        createJobInput.dataResource.totalDataCount = promoter.getTotalDataCount();
        createJobInput.dataResource.hashConfig = promoter.getHashConfigModel();

        gatewayService.callOtherPartner(
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
        partnerService.testConnection(input);
    }

}
