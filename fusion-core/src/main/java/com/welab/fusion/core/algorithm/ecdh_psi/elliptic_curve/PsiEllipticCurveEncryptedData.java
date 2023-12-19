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

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.core.algorithm.ecdh_psi.EllipticCurve;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.psi.PsiUtils;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/12/18
 */
public class PsiEllipticCurveEncryptedData {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String META_FILE_NAME = "PsiEllipticCurveEncryptedData.json";
    private static final String DATA_FILE_NAME = "PsiEllipticCurveEncryptedData.data";
    private static final String ZIP_FILE_NAME = "PsiEllipticCurveEncryptedData.zip";
    public String id;
    public HashConfig hashConfig;
    public EcdhPsiParam ecdhPsiParam;
    /**
     * 已写入数据量
     */
    public long insertedElementCount;
    /**
     * 文件所在目录，用于从磁盘中加载时使用。
     */
    private Path dir;

    public static PsiEllipticCurveEncryptedData of(String id, HashConfig hashConfig, EcdhPsiParam ecdhPsiParam) {
        PsiEllipticCurveEncryptedData data = new PsiEllipticCurveEncryptedData();
        data.id = id;
        data.hashConfig = hashConfig;
        data.ecdhPsiParam = ecdhPsiParam;
        data.dir = FileSystem.PsiEllipticCurveEncryptedData.getDir(id);
        return data;
    }

    @JSONField(serialize = false)
    public File getDataFile() {
        return dir.resolve(DATA_FILE_NAME).toFile();
    }

    public void write(String line) {
        FileUtil.writeString(
                line,
                getDataFile(),
                StandardCharsets.UTF_8
        );
    }

    /**
     * 对主键进行加密
     * 1. hash to point
     * 2. secretKey * point
     */
    public String encrypt(String key) {
        BigInteger bigIntegerValue = PsiUtils.stringToBigInteger(key);
        ECPoint point = EllipticCurve.INSTANCE.hashToPoint(bigIntegerValue);
        ECPoint encryptedValue = point.multiply(ecdhPsiParam.secretKey);
        return PsiUtils.ecPoint2String(encryptedValue);
    }
}
