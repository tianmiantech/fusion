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

import com.welab.fusion.core.data_resource.base.DataResourceType;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class FusionJobNode {

    private JobMember myself;
    private JobMember partner;

    public FusionJobNode(JobMember myself, JobMember partner) {
        this.myself = myself;
        this.partner = partner;
    }

    public void fusion() throws Exception {
        checkBeforeFusion();
        startKeeper();
    }

    /**
     * 启动监工线程
     * 监工线程负责调度
     */
    private void startKeeper() {
        while (true) {

        }
    }

    private void action(JobPhase phase) {
        switch (phase) {

        }
    }

    private void checkBeforeFusion() throws Exception {
        if (myself.dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter
                && partner.dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {
            throw new Exception("不能双方都使用布隆过滤器，建议数据量大的一方使用布隆过滤器。");
        }
    }
}
