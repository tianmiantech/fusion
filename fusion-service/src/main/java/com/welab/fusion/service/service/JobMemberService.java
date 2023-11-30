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
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.fusion.service.constans.JobMemberRole;
import com.welab.fusion.service.database.base.MySpecification;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.BloomFilterDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.repository.JobMemberRepository;
import com.welab.fusion.service.dto.JobConfigInput;
import com.welab.fusion.service.dto.JobMemberDataResourceInput;
import com.welab.fusion.service.service.base.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Date;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Service
public class JobMemberService extends AbstractService {
    @Autowired
    private JobMemberRepository jobMemberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private BloomFilterService bloomFilterService;

    /**
     * 添加发起方
     * 仅在创建任务时调用
     */
    public void addMember(JobConfigInput input) throws URISyntaxException {
        String memberId = input.fromMyselfFrontEnd()
                ? MemberService.MYSELF_NAME
                : memberService.findByUrl(input.caller.baseUrl).getId();

        JobMemberDbModel model = new JobMemberDbModel();
        model.setJobId(input.jobId);
        model.setMemberId(memberId);
        model.setRole(JobMemberRole.promoter);
        model.setDataResourceType(input.dataResource.dataResourceType);
        if (input.dataResource.tableDataResourceInput != null) {
            model.setTableDataResourceInfo(input.dataResource.tableDataResourceInput.toJson());
        }
        model.setTotalDataCount(input.dataResource.totalDataCount);
        model.setHashConfig(input.dataResource.hashConfig.toJson());
        model.save();
    }

    /**
     * 异步更新资源数据量
     */
    @Async
    public void updateTotalDataCount(JobConfigInput input) {
        if (input.fromOtherFusionNode()) {
            return;
        }

        long totalDataCount = 0;
        JobMemberDataResourceInput dataResource = input.dataResource;
        // 如果是布隆过滤器，则从布隆过滤器中获取数据量
        if (dataResource.dataResourceType == DataResourceType.PsiBloomFilter) {
            BloomFilterDbModel bloomFilter = bloomFilterService.findOneById(dataResource.bloomFilterResourceInput.bloomFilterId);
            totalDataCount = bloomFilter.getTotalDataCount();
        }
        // 如果是数据库，则从数据库中获取数据量
        else {
            try (AbstractTableDataSourceReader reader = input.dataResource.tableDataResourceInput.createReader(-1, -1)) {
                totalDataCount = reader.getTotalDataRowCount();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
        }

        JobMemberDbModel model = findMyself(input.jobId);
        if (model != null) {
            model.setTotalDataCount(totalDataCount);
            model.setUpdatedTime(new Date());
            model.save();
        }
    }

    public JobMemberDbModel findByMemberId(String jobId, String memberId) {
        if (jobId == null || memberId == null) {
            return null;
        }
        MySpecification<JobMemberDbModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("memberId", memberId)
                .build();
        return jobMemberRepository.findOne(where).orElse(null);

    }

    public JobMemberDbModel findMyself(String jobId) {
        return findByMemberId(jobId, MemberService.MYSELF_NAME);
    }
}
