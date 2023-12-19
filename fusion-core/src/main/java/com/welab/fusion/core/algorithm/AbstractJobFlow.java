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
package com.welab.fusion.core.algorithm;

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.algorithm.base.AbstractJobPhaseAction;

import java.util.List;
import java.util.Map;

/**
 * 任务流程声明
 *
 * @author zane.luo
 * @date 2023/12/18
 */
public abstract class AbstractJobFlow {
    private List<JobPhase> flow;
    private Map<JobPhase, Class<? extends AbstractJobPhaseAction>> phaseActionMap;

    public AbstractJobFlow(List<JobPhase> flow,Map<JobPhase, Class<? extends AbstractJobPhaseAction>> actionMap) {
        this.flow = flow;
        this.phaseActionMap = actionMap;
    }

    /**
     * 获取下一个阶段
     */
    public JobPhase nextPhase(JobPhase phase) {
        int index = phaseIndex(phase);
        if (index == flow.size() - 1) {
            return null;
        }
        return flow.get(index + 1);
    }

    /**
     * 获取按顺序排列的所有阶段
     */
    public List<JobPhase> listPhase() {
        return flow;
    }

    /**
     * 获取第一个阶段
     */
    public JobPhase firstPhase() {
        return flow.get(0);
    }

    public int phaseIndex(JobPhase phase) {
        return flow.indexOf(phase);
    }

    /**
     * 是否是最后一个阶段
     */
    public boolean isLastPhase(JobPhase phase) {
        return nextPhase(phase) == null;
    }

    /**
     * 创建任务阶段动作
     */
    public AbstractJobPhaseAction createAction(JobPhase phase, FusionJob fusionJob) {
        Class<? extends AbstractJobPhaseAction> aClass = phaseActionMap.get(phase);
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
