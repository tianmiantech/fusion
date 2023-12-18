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
package com.welab.fusion.core.algorithm.ecdh_psi.elliptic_curve;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.hash.BloomFilter;
import com.welab.fusion.core.hash.HashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/12/18
 */
public class PsiEllipticCurve {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String META_FILE_NAME = "PsiEllipticCurve.json";
    private static final String DATA_FILE_NAME = "PsiEllipticCurve.data";
    private static final String ZIP_FILE_NAME = "PsiEllipticCurve.zip";
    public String id;
    public HashConfig hashConfig;
    public EcdhPsiParam EcdhPsiParam;
    /**
     * 已写入数据量
     */
    public long insertedElementCount;
    /**
     * 文件所在目录，用于从磁盘中加载时使用。
     */
    private Path dir;
}
