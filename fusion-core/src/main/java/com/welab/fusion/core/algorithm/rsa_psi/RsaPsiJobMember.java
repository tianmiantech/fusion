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

import com.welab.fusion.core.Job.AbstractJobMember;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.wefe.common.util.CloseableUtils;

import java.io.IOException;

/**
 * @author zane.luo
 * @date 2023/12/20
 */
public class RsaPsiJobMember extends AbstractJobMember {
    public PsiBloomFilter psiBloomFilter;

    public RsaPsiJobMember(String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        super(memberId, memberName, dataResourceInfo);
    }

    public static RsaPsiJobMember of(String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        return new RsaPsiJobMember(memberId, memberName, dataResourceInfo);
    }

    public static RsaPsiJobMember of(String memberId, String memberName, PsiBloomFilter psiBloomFilter) {
        DataResourceInfo dataResourceInfo = DataResourceInfo.of(DataResourceType.PsiBloomFilter, psiBloomFilter.insertedElementCount, psiBloomFilter.hashConfig);

        RsaPsiJobMember jobMember = of(memberId, memberName, dataResourceInfo);
        jobMember.psiBloomFilter = psiBloomFilter;
        return jobMember;
    }


}
