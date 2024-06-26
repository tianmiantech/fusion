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

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/14
 */
@FunctionalInterface
public interface EncryptRsaPsiRecordsFunction {
    /**
     * 加密数据
     *
     * @param partnerId 合作方id
     * @param bucket    待加密的数据
     * @return 加密后的数据
     */
    List<String> encrypt(String jobId, String partnerId, List<String> bucket) throws Exception;

}
