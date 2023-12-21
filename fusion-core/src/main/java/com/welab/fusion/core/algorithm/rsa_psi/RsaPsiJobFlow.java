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

import com.welab.fusion.core.algorithm.AbstractJobFlow;
import com.welab.fusion.core.algorithm.JobPhase;
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;
import com.welab.fusion.core.algorithm.rsa_psi.action.*;

import java.util.LinkedHashMap;

/**
 * @author zane.luo
 * @date 2023/12/18
 */
public class RsaPsiJobFlow extends AbstractJobFlow {
    private static final LinkedHashMap<JobPhase, Class<? extends AbstractJobPhaseAction>> map = new LinkedHashMap<>();

    static {
        map.put(JobPhase.ConfirmMemberRole, P1ConfirmMemberRoleAction.class);
        map.put(JobPhase.CreatePsiBloomFilter, P2CreatePsiBloomFilterAction.class);
        map.put(JobPhase.DownloadPsiBloomFilter, P3DownloadPsiBloomFilterAction.class);
        map.put(JobPhase.Intersection, P4IntersectionAction.class);
        map.put(JobPhase.SaveResult, P5SaveResultAction.class);
    }

    public static final RsaPsiJobFlow INSTANCE = new RsaPsiJobFlow();

    public RsaPsiJobFlow() {
        super(map);
    }
}
