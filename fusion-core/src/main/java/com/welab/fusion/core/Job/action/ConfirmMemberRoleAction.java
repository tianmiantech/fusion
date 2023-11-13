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
package com.welab.fusion.core.Job.action;

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.Job.JobMember;
import com.welab.fusion.core.Job.JobPhase;
import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.core.data_resource.base.DataResourceType;

/**
 * 确认成员角色
 * 这里采用了离线协商的方式
 *
 * @author zane.luo
 * @date 2023/11/13
 */
public class ConfirmMemberRoleAction extends AbstractJobPhaseAction {
    public ConfirmMemberRoleAction(FusionJob job) {
        super(job);
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }

    @Override
    public void doAction() throws Exception {
        job.setMyRole(consultMyRole());
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.ConfirmMemberRole;
    }

    @Override
    public long getTotalWorkload() {
        return 1;
    }

    /**
     * 协商我方身份
     *
     * [规则]
     * 双方资源类型不同：
     * - 一方为过滤器，一方为数据集，无需判定。
     *
     * 双方资源类型相同：
     * - 双方都为数据集，数据量大的生成过滤器。
     * - 双方都为数据集，双方数据量相同，成员名称字典序小的生成过滤器。
     */
    private FusionJobRole consultMyRole() throws Exception {
        JobMember myself = job.getMyself();
        JobMember partner = job.getPartner();
        DataResourceInfo myDataResourceInfo = myself.dataResourceInfo;
        DataResourceInfo partnerDataResourceInfo = partner.dataResourceInfo;


        if (myDataResourceInfo.dataResourceType == partnerDataResourceInfo.dataResourceType) {
            return consultMyRoleWhenEqualType();
        } else {
            return consultMyRoleWhenNotEqualType();
        }


    }

    /**
     * 双方资源类型相同
     */
    private FusionJobRole consultMyRoleWhenEqualType() throws Exception {
        JobMember myself = job.getMyself();
        JobMember partner = job.getPartner();
        DataResourceInfo myDataResourceInfo = myself.dataResourceInfo;
        DataResourceInfo partnerDataResourceInfo = partner.dataResourceInfo;

        // 双方都是过滤器，不支持。
        if (myDataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {
            throw new Exception("不支持双方都为过滤器");
        }

        // 双方数据量相同，根据名称 hash code 判定身份。
        // 这种情况角色分配不重要，重要的是多方判定结果一致。
        if (myDataResourceInfo.dataCount == partnerDataResourceInfo.dataCount) {
            int myHash = myself.memberName.hashCode();
            int partnerHash = partner.memberName.hashCode();
            if (myHash == partnerHash) {
                throw new Exception("双方成员名称不能相同");
            }

            return myHash > partnerHash
                    ? FusionJobRole.psi_bool_filter
                    : FusionJobRole.table_data_resource;
        }

        // 双方数据量不同，数据量大的生成过滤器。
        return myDataResourceInfo.dataCount > partnerDataResourceInfo.dataCount
                ? FusionJobRole.psi_bool_filter
                : FusionJobRole.table_data_resource;
    }

    /**
     * 双方资源类型不同
     */
    private FusionJobRole consultMyRoleWhenNotEqualType() {
        switch (job.getMyself().dataResourceInfo.dataResourceType) {
            case PsiBloomFilter:
                return FusionJobRole.psi_bool_filter;

            case TableDataSource:
                return FusionJobRole.table_data_resource;
            default:
                throw new RuntimeException("意料之外的情形");
        }
    }
}
