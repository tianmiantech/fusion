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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public enum JobPhase {
    /**
     * 确认成员角色
     */
    ConfirmMemberRole(0),
    /**
     * 生成过滤器
     */
    CreatePsiBloomFilter(1),
    /**
     * 下载过滤器
     */
    DownloadPsiBloomFilter(2),
    /**
     * 求交
     */
    Intersection(3),
    /**
     * 拼接扩展字段到求交结果
     */
    AppendExtendedFieldToResult(4),
    /**
     * 下载结果
     */
    DownloadResult(5);
    private static final List<JobPhase> sortedList;

    static {
        sortedList = Arrays.stream(JobPhase.values())
                .sorted(Comparator.comparingInt(o -> o.phaseIndex))
                .collect(Collectors.toList());
    }

    private int phaseIndex;

    JobPhase(int phaseIndex) {
        this.phaseIndex = phaseIndex;
    }

    /**
     * 获取下一个阶段
     */
    public JobPhase next() {
        if (phaseIndex == sortedList.size() - 1) {
            return null;
        }
        return sortedList.get(phaseIndex + 1);
    }

    /**
     * 获取按顺序排列的所有阶段
     */
    public static List<JobPhase> list() {
        return sortedList;
    }

    /**
     * 获取第一个阶段
     */
    public static JobPhase firstPhase() {
        return JobPhase.CreatePsiBloomFilter;
    }

    public int index() {
        return phaseIndex;
    }

    /**
     * 是否是最后一个阶段
     */
    public boolean isLastPhase() {
        return next() == null;
    }
}
