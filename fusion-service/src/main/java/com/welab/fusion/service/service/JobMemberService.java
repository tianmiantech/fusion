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

import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.service.api.job.CreateJobApi;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.base.MySpecification;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.BloomFilterDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.repository.JobMemberRepository;
import com.welab.fusion.service.service.base.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Service
public class JobMemberService extends AbstractService {
    @Autowired
    private JobMemberRepository jobMemberRepository;
    @Autowired
    private PartnerService partnerService;


    /**
     * 添加发起方
     * 仅在创建任务时调用
     */
    public void addPromoter(CreateJobApi.Input input) throws URISyntaxException {
        String partnerId = input.fromMyselfFrontEnd()
                ? PartnerService.MYSELF_NAME
                : partnerService.findByUrl(input.partnerCaller.baseUrl).getId();

        JobMemberDbModel model = new JobMemberDbModel();
        model.setJobId(input.jobId);
        model.setPartnerId(partnerId);
        model.setRole(JobMemberRole.promoter);
        model.setDataResourceType(input.dataResourceType);
        model.setTotalDataCount(input.totalDataCount);
        model.setHashConfig(input.hashConfig.toJson());

        model.save();
    }

    @Autowired
    private BloomFilterService bloomFilterService;

    @Async
    public void updateTotalDataCount(CreateJobApi.Input input) {
        long totalDataCount = 0;
        if (input.dataResourceType == DataResourceType.PsiBloomFilter) {
            BloomFilterDbModel bloomFilter = bloomFilterService.findOneById(input.bloomFilterResourceInput.bloomFilterId);
            totalDataCount = bloomFilter.getTotalDataCount();
        }

        JobMemberDbModel model = findMyself(input.jobId);
        if (model != null) {
            model.setTotalDataCount(totalDataCount);
            model.save();
        }
    }

    public JobMemberDbModel findByPartnerId(String jobId, String partnerId) {
        MySpecification<JobMemberDbModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("partnerId", partnerId)
                .build();
        return jobMemberRepository.findOne(where).orElse(null);

    }

    public JobMemberDbModel findMyself(String jobId) {
        return findByPartnerId(jobId, PartnerService.MYSELF_NAME);
    }
}
