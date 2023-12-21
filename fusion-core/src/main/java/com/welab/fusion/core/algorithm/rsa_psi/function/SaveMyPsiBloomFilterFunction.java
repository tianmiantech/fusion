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
package com.welab.fusion.core.algorithm.rsa_psi.function;

import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.wefe.common.exception.StatusCodeWithException;

import java.io.IOException;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
@FunctionalInterface
public interface SaveMyPsiBloomFilterFunction {
    /**
     * 保存我方新生成的 PSI 过滤器
     */
    void save(String jobId, PsiBloomFilter psiBloomFilter) throws StatusCodeWithException, IOException, Exception;
}