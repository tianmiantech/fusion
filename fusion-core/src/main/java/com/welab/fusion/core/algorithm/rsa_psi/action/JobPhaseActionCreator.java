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
package com.welab.fusion.core.algorithm.rsa_psi.action;

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.algorithm.rsa_psi.JobPhase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class JobPhaseActionCreator {
    private static final Map<JobPhase, Class<? extends AbstractJobPhaseAction>> map = new HashMap<>();

    static {
        map.put(JobPhase.ConfirmMemberRole, ConfirmMemberRoleAction.class);
        map.put(JobPhase.CreatePsiBloomFilter, CreatePsiBloomFilterAction.class);
        map.put(JobPhase.DownloadPsiBloomFilter, DownloadPsiBloomFilterAction.class);
        map.put(JobPhase.Intersection, IntersectionAction.class);
        map.put(JobPhase.SaveResult, SaveResultAction.class);

    }

    public static AbstractJobPhaseAction create(JobPhase phase, FusionJob fusionJob) {
        Class<? extends AbstractJobPhaseAction> aClass = map.get(phase);
        if (aClass == null) {
            throw new RuntimeException("Unknown phase: " + phase);
        }

        try {
            return aClass.getConstructor(FusionJob.class).newInstance(fusionJob);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
