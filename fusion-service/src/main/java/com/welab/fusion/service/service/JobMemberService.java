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
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.entity.PartnerDbModel;
import com.welab.fusion.service.database.repository.JobMemberRepository;
import com.welab.fusion.service.service.base.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void addMyself(String jobId, CreateJobApi.Input input) {
        // TODO
    }

    public void addPromoter(CreateJobApi.Input input) throws URISyntaxException {

        PartnerDbModel promoter = partnerService.findByUrl(input.partnerCaller.baseUrl);

        JobMemberDbModel model = new JobMemberDbModel();
        model.setJobId(input.jobId);
        model.setPartnerId(promoter.getId());
        model.setRole(JobMemberRole.promoter);
        model.setDataResourceType(input.dataResourceType);
        model.setTotalDataCount(input.totalDataCount);
        model.setHashConfigs(input.hashConfig.toJson());

        model.save();
    }
}
