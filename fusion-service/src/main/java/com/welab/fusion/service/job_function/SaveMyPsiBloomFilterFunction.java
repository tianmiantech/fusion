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
package com.welab.fusion.service.job_function;

import com.welab.fusion.core.Job.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.service.api.job.CreateJobApi;
import com.welab.fusion.service.constans.AddMethod;
import com.welab.fusion.service.database.entity.BloomFilterDbModel;
import com.welab.fusion.service.database.entity.JobMemberDbModel;
import com.welab.fusion.service.service.BloomFilterService;
import com.welab.fusion.service.service.JobMemberService;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.web.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class SaveMyPsiBloomFilterFunction implements com.welab.fusion.core.Job.algorithm.rsa_psi.function.SaveMyPsiBloomFilterFunction {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final JobMemberService jobMemberService = Launcher.getBean(JobMemberService.class);
    private static final BloomFilterService bloomFilterService = Launcher.getBean(BloomFilterService.class);

    @Override
    public void save(String jobId, PsiBloomFilter psiBloomFilter) throws Exception {
        int key = psiBloomFilter.hashCode();

        /**
         * 暂时不考虑去重
         * 以后稳定了再考虑
         */
        // 避免储存重复的过滤器
        // BloomFilterDbModel existed = bloomFilterService.findAutoGenerateByKey(key);
        // if (existed != null) {
        //     LOG.info("过滤器已存在，不重复储存。 job_id：{}，bloom_filter_id:{}，key{}", jobId, psiBloomFilter.id, key);
        //     return;
        // }

        LOG.info("开始保存过滤器文件，job_id：{}，bloom_filter_id:{}", jobId, psiBloomFilter.id);
        long startTime = System.currentTimeMillis();
        psiBloomFilter.sink();
        TimeSpan spend = TimeSpan.fromMs(System.currentTimeMillis() - startTime);
        LOG.info("保存过滤器文件完成，job_id：{}，bloom_filter_id:{}，耗时：{}", jobId, psiBloomFilter.id, spend);

        LOG.info("开始压缩过滤器文件，job_id：{}，bloom_filter_id:{}", jobId, psiBloomFilter.id);
        startTime = System.currentTimeMillis();
        psiBloomFilter.zip();
        spend = TimeSpan.fromMs(System.currentTimeMillis() - startTime);
        LOG.info("压缩过滤器文件完成，job_id：{}，bloom_filter_id:{}，耗时：{}", jobId, psiBloomFilter.id, spend);


        // 更新自己的数据源类型
        JobMemberDbModel myself = jobMemberService.findMyself(jobId);
        myself.setDataResourceType(DataResourceType.PsiBloomFilter);
        myself.setBloomFilterId(psiBloomFilter.id);
        myself.setUpdatedTimeNow();
        myself.save();

        // 保存过滤器，供以后复用。
        CreateJobApi.TableDataResourceInput tableDataResourceInfoModel = myself.getTableDataResourceInfoModel();
        BloomFilterDbModel model = new BloomFilterDbModel();
        model.setId(psiBloomFilter.id);
        model.setName(tableDataResourceInfoModel.buildAutoName() + "(自动生成)");
        model.setAddMethod(AddMethod.AutoGenerate);
        model.setDescription(tableDataResourceInfoModel.buildAutoDescription());
        model.setSql(tableDataResourceInfoModel.sql);
        model.setHashConfig(myself.getHashConfig());
        model.setTotalDataCount(psiBloomFilter.insertedElementCount);
        model.setStorageDir(psiBloomFilter.getDir().toAbsolutePath().toString());
        model.setStorageSize(psiBloomFilter.getDataFile().length());
        model.setKey(key + "");
        model.save();
    }
}
