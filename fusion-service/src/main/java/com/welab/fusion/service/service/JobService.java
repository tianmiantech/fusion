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
import com.welab.fusion.service.service.base.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public String createJob(CreateJobApi.Input input) throws Exception {
        checkBeforeCreateJob(input);
        // 自动保存合作方信息
        partnerService.trySave(input.partnerCaller);

        JobDbModel job = new JobDbModel();
        // 来自自己前端，填充任务Id，便于其它方法他统一行为。
        if (input.fromMyselfFrontEnd()) {
            input.jobId = job.getId();
        }

        // 来自发起方，填充合作者信息。
        if (input.fromPartner()) {
            String promoterId = PartnerService.buildPartnerId(input.partnerCaller.baseUrl);
            PartnerDbModel promoter = partnerService.findById(promoterId);

            job.setPartnerId(promoterId);
            job.setPartnerName(promoter.getName());
        }

        job.setId(input.jobId);
        job.setRole(input.fromPartner() ? JobMemberRole.provider : JobMemberRole.promoter);
        job.setRemark(input.remark);

        jobMemberService.addPromoter(input);
        jobRepository.save(job);

        return job.getId();
    }

    private void checkBeforeCreateJob(CreateJobApi.Input input) {

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
        String providerId = PartnerService.buildPartnerId(input.partnerCaller.baseUrl);
        job.setPartnerId(providerId);
        PartnerDbModel provider = partnerService.findById(providerId);
        if (provider != null) {
            job.setPartnerName(provider.getName());
        }

        JobMemberDbModel promoter = jobMemberService.findMyself(job.getId());

        CreateJobApi.Input createJobInput = new CreateJobApi.Input();
        createJobInput.jobId = job.getId();
        createJobInput.dataResourceType = promoter.getDataResourceType();
        createJobInput.totalDataCount = promoter.getTotalDataCount();
        createJobInput.hashConfig = promoter.getHashConfigModel();

        gatewayService.callOtherPartner(CreateJobApi.class, createJobInput);
    }

    /**
     * 发送任务前的检查
     */
    private void checkBeforeSendJob(SendJobApi.Input input) throws Exception {
        // 检查连通性
        partnerService.testConnection(input);
    }
}
