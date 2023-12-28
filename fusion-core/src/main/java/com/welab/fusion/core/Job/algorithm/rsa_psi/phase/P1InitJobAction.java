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
package com.welab.fusion.core.Job.algorithm.rsa_psi.phase;

import com.welab.fusion.core.Job.algorithm.rsa_psi.RsaPsiJob;
import com.welab.fusion.core.Job.base.AbstractJobMember;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.algorithm.base.phase_action.AbstractInitJobAction;
import com.welab.fusion.core.Job.data_resource.DataResourceInfo;
import com.welab.fusion.core.Job.data_resource.DataResourceType;

/**
 * 确认成员角色
 * 这里采用了离线协商的方式
 *
 * leader：提供数据集，提供返回字段。
 * follower: 提供过滤器
 *
 * @author zane.luo
 * @date 2023/11/13
 */
public class P1InitJobAction extends AbstractInitJobAction<RsaPsiJob> {
    public P1InitJobAction(RsaPsiJob job) {
        super(job);
    }

    @Override
    public void doAction() throws Exception {
        JobRole role = consultMyRole();
        job.setMyRole(role);
        phaseProgress.setMessage("协商完毕，我方角色：" + role);

        super.loadOriginalDataToJobWorkspace();
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
    private JobRole consultMyRole() throws Exception {
        AbstractJobMember myself = job.getMyself();
        AbstractJobMember partner = job.getPartner();
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
    private JobRole consultMyRoleWhenEqualType() throws Exception {
        AbstractJobMember myself = job.getMyself();
        AbstractJobMember partner = job.getPartner();
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
                    ? JobRole.follower
                    : JobRole.leader;
        }

        // 双方数据量不同，数据量大的生成过滤器。
        return myDataResourceInfo.dataCount > partnerDataResourceInfo.dataCount
                ? JobRole.follower
                : JobRole.leader;
    }

    /**
     * 双方资源类型不同
     */
    private JobRole consultMyRoleWhenNotEqualType() {
        switch (job.getMyself().dataResourceInfo.dataResourceType) {
            case PsiBloomFilter:
                return JobRole.follower;

            case TableDataSource:
                return JobRole.leader;
            default:
                throw new RuntimeException("意料之外的情形");
        }
    }
}
