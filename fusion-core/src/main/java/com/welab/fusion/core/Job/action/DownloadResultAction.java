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
package com.welab.fusion.core.Job.action;

import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.Job.JobPhase;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class DownloadResultAction extends AbstractJobPhaseAction {
    public DownloadResultAction(FusionJob job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {

    }

    @Override
    public JobPhase getPhase() {
        return null;
    }

    @Override
    public long getTotalWorkload() {
        return 0;
    }

    @Override
    protected boolean skipThisAction() {
        return false;
    }
}
