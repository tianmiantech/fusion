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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.psi.PsiUtils;
import com.welab.wefe.common.file.compression.impl.Zip;
import com.welab.wefe.common.util.JObject;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/12/18
 */
public class PsiECEncryptedData {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String META_FILE_NAME = "PsiECEncryptedData.json";
    private static final String DATA_FILE_NAME = "PsiECEncryptedData.data";
    private static final String ZIP_FILE_NAME = "PsiECEncryptedData.zip";
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

    public static PsiECEncryptedData of(String id, HashConfig hashConfig, EcdhPsiParam ecdhPsiParam) {
        PsiECEncryptedData data = new PsiECEncryptedData();
        data.id = id;
        data.hashConfig = hashConfig;
        data.ecdhPsiParam = ecdhPsiParam;
        data.dir = FileSystem.PsiECEncryptedData.getDir(id);
        return data;
    }

    public static PsiECEncryptedData of(String id) {
        Path dir = FileSystem.PsiECEncryptedData.getDir(id);

        return of(dir);
    }

    public static PsiECEncryptedData of(Path dir) {

        // 加载元数据
        File metaFile = dir.resolve(META_FILE_NAME).toFile();
        String json = FileUtil.readString(metaFile, StandardCharsets.UTF_8);
        PsiECEncryptedData result = JSON.parseObject(json).toJavaObject(PsiECEncryptedData.class);
        result.dir = dir;
        return result;
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
     * 第一次加密：对主键进行加密
     * 1. hash to point
     * 2. secretKey * point
     */
    public String encryptMyselfData(String key) {
        BigInteger bigIntegerValue = PsiUtils.stringToBigInteger(key);
        ECPoint point = EllipticCurve.INSTANCE.hashToPoint(bigIntegerValue);
        ECPoint encryptedValue = point.multiply(ecdhPsiParam.secretKey);
        return PsiUtils.ecPoint2String(encryptedValue);
    }

    /**
     * 第二次加密：对合作方的数据进行加密
     */
    public String encryptPartnerData(String base64) {
        ECPoint point = EllipticCurve.INSTANCE.base64ToECPoint(base64);
        ECPoint encryptedValue = point.multiply(ecdhPsiParam.secretKey);
        return PsiUtils.ecPoint2String(encryptedValue);
    }

    /**
     * 将 meta 文件与 data 文件打包成 zip 文件
     *
     * 这里要注意： meta 文件中对象包含私钥信息，打包前要删除。
     */
    public File zip() throws IOException {
        Path dir = FileSystem.PsiECEncryptedData.getDir(id);
        File zipFile = dir.resolve(ZIP_FILE_NAME).toFile();

        // 已存在，不反复压缩。
        if (zipFile.exists()) {
            return zipFile;
        }

        // 抹除私钥信息，只保留公钥信息。
        // 为了避免影响旧对象，先拷贝一个新的。
        PsiECEncryptedData newPsiECEncryptedData = JObject.create(this).toJavaObject(PsiECEncryptedData.class);
        newPsiECEncryptedData.ecdhPsiParam.cleanPrivateKey();

        // 抹除后输出到临时目录
        File tempMetaFile = dir.resolve("temp").resolve(META_FILE_NAME).toFile();
        tempMetaFile.getParentFile().mkdirs();
        Files.write(tempMetaFile.toPath(), JSON.toJSONBytes(newPsiECEncryptedData));

        // 压缩文件
        File dataFile = dir.resolve(DATA_FILE_NAME).toFile();
        Zip.to(zipFile, tempMetaFile, dataFile);

        return zipFile;

    }

    public void sink() {
        // 输出元数据
        File metaFile = dir.resolve(META_FILE_NAME).toFile();
        try {
            Files.write(metaFile.toPath(), JSON.toJSONBytes(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
