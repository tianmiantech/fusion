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
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJobMember;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJobMember;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.job.CreateJobApi;
import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.database.entity.MemberDbModel;
import com.welab.fusion.service.job_function.MyEcdhJobFunctions;
import com.welab.fusion.service.job_function.MyRsaJobFunctions;
import com.welab.fusion.service.service.base.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 * @date 2023/12/25
 */
@Service
public class CreatePsiJobService extends AbstractService {
    @Autowired
    private MemberService memberService;
    @Autowired
    private JobMemberService jobMemberService;

    public AbstractPsiJob createPsiJob(JobDbModel job) throws Exception {
        JobMemberDbModel myselfJobInfo = jobMemberService.findMyself(job.getId());
        MemberDbModel myselfInfo = memberService.getMyself();

        JobMemberDbModel partnerJobInfo = jobMemberService.findByMemberId(job.getId(), job.getPartnerMemberId());
        MemberDbModel partnerInfo = memberService.findById(partnerJobInfo.getMemberId());

        switch (job.getAlgorithm()) {
            case rsa_psi:
                return new RsaPsiJob(
                        job.getId(),
                        createRsaJobMember(myselfInfo, myselfJobInfo),
                        createRsaJobMember(partnerInfo, partnerJobInfo),
                        MyRsaJobFunctions.INSTANCE
                );

            case ecdh_psi:
                return new EcdhPsiJob(
                        job.getId(),
                        createEcdhJobMember(myselfInfo, myselfJobInfo),
                        createEcdhJobMember(partnerInfo, partnerJobInfo),
                        MyEcdhJobFunctions.INSTANCE
                );

            default:
                throw new RuntimeException("Unknown algorithm: " + job.getAlgorithm());
        }

    }

    public EcdhPsiJobMember createEcdhJobMember(MemberDbModel member, JobMemberDbModel jobMember) throws Exception {
        DataResourceInfo dataResourceInfo = DataResourceInfo.of(
                jobMember.getDataResourceType(),
                jobMember.getTotalDataCount(),
                jobMember.getHashConfigModel()
        );

        EcdhPsiJobMember result = EcdhPsiJobMember.of(member.getId(), member.getName(), dataResourceInfo);
        if (member.isMyself() && dataResourceInfo.dataResourceType == DataResourceType.TableDataSource) {
            CreateJobApi.TableDataResourceInput tableDataResourceInfoModel = jobMember.getTableDataResourceInfoModel();
            result.tableDataResourceReader = tableDataResourceInfoModel.createReader(-1, -1);
        }

        return result;
    }

    public RsaPsiJobMember createRsaJobMember(MemberDbModel member, JobMemberDbModel jobMember) throws Exception {
        DataResourceInfo dataResourceInfo = DataResourceInfo.of(
                jobMember.getDataResourceType(),
                jobMember.getTotalDataCount(),
                jobMember.getHashConfigModel()
        );
        RsaPsiJobMember result = RsaPsiJobMember.of(member.getId(), member.getName(), dataResourceInfo);
        if (dataResourceInfo.dataResourceType == DataResourceType.TableDataSource) {
            CreateJobApi.TableDataResourceInput tableDataResourceInfoModel = jobMember.getTableDataResourceInfoModel();
            result.tableDataResourceReader = tableDataResourceInfoModel.createReader(-1, -1);
        }


        if (member.isMyself() && dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {
            if (!PsiBloomFilter.exist(jobMember.getBloomFilterId())) {
                throw new RuntimeException(
                        "过滤器文件不存在或已损坏："
                                + FileSystem.PsiBloomFilter.getDir(jobMember.getBloomFilterId())
                );
            }
            result.psiBloomFilter = PsiBloomFilter.of(
                    jobMember.getBloomFilterId()
            );
        }

        return result;
    }
}
