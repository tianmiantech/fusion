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

import cn.hutool.core.util.StrUtil;
import com.welab.fusion.core.data_resource.base.DataResourceType;

import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class FusionJob {

    private String jobId;
    private JobMember myself;
    private JobMember partner;
    JobProgress myProgress = new JobProgress();


    public FusionJob(String jobId, JobMember myself, JobMember partner) {
        this.jobId = StrUtil.isEmpty(jobId)
                ? UUID.randomUUID().toString().replace("-", "")
                : jobId;
        this.myself = myself;
        this.partner = partner;

        myProgress.init(jobId, myself);
    }

    public void fusion() throws Exception {
        checkBeforeFusion();
        startKeeper();
    }

    /**
     * 启动监工线程
     * 监工线程负责调度
     *
     * 这里采取观察者模式，各方根据观察到的信息调度自己的任务。
     * 不对其它方的任务进行主动通知。
     */
    private void startKeeper() {
        while (true) {
            JobProgress partnerProgress = getPartnerProgress();
            if (partnerProgress == null) {
                finishJobByDisconnection();
            }

            // 如果对方已失败，则我方跟随失败。
            if (partnerProgress.getStatus().isFailed()) {
                finishJobFollowFailed(partnerProgress);
                break;
            }

            // 如果双方处于同一阶段，且双方都已完成，则进入下一阶段。
            if (myProgress.getCurrentPhase() == partnerProgress.getCurrentPhase()) {
            }
        }
    }

    /**
     * 因与合作方断线失联引起的任务中止
     */
    private void finishJobByDisconnection() {
        String message = "与成员[" + partner.memberName + "]失联，我方停止任务。";
        finishJob(JobStatus.error_on_running, message);
    }

    /**
     * 跟随合作伙伴失败
     */
    private void finishJobFollowFailed(JobProgress partnerProgress) {
        String message = "成员[" + partner.memberName + "]已中止任务，我方跟随其中止。";
        finishJob(JobStatus.error_on_running, message);
    }

    /**
     * 结束任务
     */
    private void finishJob(JobStatus status, String message) {
        myProgress.finish(status, message);
    }

    /**
     * 获取合作伙伴的任务进度
     */
    private JobProgress getPartnerProgress() {
        return null;
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
