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
package com.welab.fusion.core.Job;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class JobMember {
    public String memberId;
    public String memberName;
    public DataResourceInfo dataResourceInfo;

    @JSONField(serialize = false)
    public AbstractTableDataSourceReader tableDataResourceReader;

    @JSONField(serialize = false)
    public PsiBloomFilter psiBloomFilter;
    @JSONField(serialize = false)
    public PsiECEncryptedData psiECEncryptedData;

    public static JobMember of(String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        JobMember jobMember = new JobMember();
        jobMember.memberId = memberId;
        jobMember.memberName = memberName;
        jobMember.dataResourceInfo = dataResourceInfo;
        return jobMember;
    }

    public static JobMember of(String memberId, String memberName, PsiBloomFilter psiBloomFilter) {
        JobMember jobMember = new JobMember();
        jobMember.memberId = memberId;
        jobMember.memberName = memberName;
        jobMember.psiBloomFilter = psiBloomFilter;
        return jobMember;
    }
}
