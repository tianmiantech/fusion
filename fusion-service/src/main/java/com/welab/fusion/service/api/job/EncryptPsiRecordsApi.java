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
package com.welab.fusion.service.api.job;

import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.psi.PsiUtils;
import com.welab.fusion.service.service.JobMemberService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zane.luo
 * @date 2023/11/30
 */
@Api(
        path = "job/encrypt_psi_records",
        name = "使用过滤器配套的秘钥加密融合数据",
        desc = "在算法执行过程中，使用数据集的一方会把 Id 加密后发送给过滤器方进行加密。",
        allowAccessWithSign = true)
public class EncryptPsiRecordsApi extends AbstractApi<EncryptPsiRecordsApi.Input, EncryptPsiRecordsApi.Output> {
    /**
     * 过滤器缓存
     *
     * 在一次任务过程中，会分批次的发送 Id 给过滤器方进行加密，每一个批次会调用一次这个接口。
     * 为避免反复从硬盘加载过滤器，使用缓存。
     */
    private static ExpiringMap<String, PsiBloomFilter> CACHE = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(5, TimeUnit.MINUTES)
            // 考虑到过滤器比较占内存，最多缓存 5 个过滤器。
            .maxSize(5)
            .build();

    @Autowired
    private JobMemberService jobMemberService;

    @Override
    protected ApiResult<EncryptPsiRecordsApi.Output> handle(EncryptPsiRecordsApi.Input input) throws Exception {
        // 找到这个任务对应的过滤器
        String bloomFilterId = jobMemberService.findMyself(input.jobId).getBloomFilterId();

        PsiBloomFilter psiBloomFilter = CACHE.containsKey(bloomFilterId)
                // 从缓存加载过滤器
                ? CACHE.get(bloomFilterId)
                // 从硬盘加载过滤器
                : PsiBloomFilter.of(FileSystem.PsiBloomFilter.getPath(bloomFilterId));

        // 使用过滤器加密数据
        List<String> list = PsiUtils.encryptPsiRecords(psiBloomFilter, input.bucket);
        return success(Output.of(list));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        public String jobId;
        @Check(name = "一批加密后的 Id", require = true)
        public List<String> bucket;

        public static AbstractApiInput of(String jobId, List<String> bucket) {
            Input input = new Input();
            input.jobId = jobId;
            input.bucket = bucket;
            return input;
        }
    }

    public static class Output {
        @Check(name = "一批二次加密后的 Id", require = true)
        public List<String> bucket;

        public static Output of(List<String> bucket) {
            Output output = new Output();
            output.bucket = bucket;
            return output;
        }
    }
}
