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
package com.welab.fusion.core.algorithm.rsa_psi;

import com.welab.fusion.core.Job.base.AbstractJobMember;
import com.welab.fusion.core.Job.data_resource.DataResourceInfo;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;

/**
 * @author zane.luo
 * @date 2023/12/20
 */
public class RsaPsiJobMember extends AbstractJobMember {
    public PsiBloomFilter psiBloomFilter;

    public RsaPsiJobMember(boolean isPromoter, String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        super(isPromoter, memberId, memberName, dataResourceInfo);
    }

    public static RsaPsiJobMember of(boolean isPromoter, String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        return new RsaPsiJobMember(isPromoter, memberId, memberName, dataResourceInfo);
    }

    public static RsaPsiJobMember of(boolean isPromoter, String memberId, String memberName, PsiBloomFilter psiBloomFilter) {
        DataResourceInfo dataResourceInfo = DataResourceInfo.of(
                DataResourceType.PsiBloomFilter,
                psiBloomFilter.insertedElementCount,
                psiBloomFilter.hashConfig,
                null
        );

        RsaPsiJobMember jobMember = of(isPromoter, memberId, memberName, dataResourceInfo);
        jobMember.psiBloomFilter = psiBloomFilter;
        return jobMember;
    }


}
