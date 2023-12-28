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
package com.welab.fusion.core.Job.algorithm.ecdh_psi;

import com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.fusion.core.Job.base.AbstractJobMember;
import com.welab.fusion.core.Job.data_resource.DataResourceInfo;

import java.io.File;

/**
 * @author zane.luo
 * @date 2023/12/20
 */
public class EcdhPsiJobMember extends AbstractJobMember {
    /**
     * 经椭圆曲线加密后的数据文件
     */
    public PsiECEncryptedData psiECEncryptedData;
    /**
     * 二次加密后的数据文件
     */
    public File secondaryECEncryptedDataFile;

    public EcdhPsiJobMember(String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        super(memberId, memberName, dataResourceInfo);
    }

    public static EcdhPsiJobMember of(String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        return new EcdhPsiJobMember(memberId, memberName, dataResourceInfo);
    }

}
