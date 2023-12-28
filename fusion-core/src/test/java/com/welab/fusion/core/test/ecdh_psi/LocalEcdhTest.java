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
package com.welab.fusion.core.test.ecdh_psi;

import com.google.common.io.Files;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve.EllipticCurve;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedData;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.elliptic_curve.PsiECEncryptedDataCreator;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.PsiUtils;
import com.welab.wefe.common.util.FileUtil;
import org.bouncycastle.math.ec.ECPoint;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
public class LocalEcdhTest {

    private static HashConfig hashConfig = HashConfig.of(HashConfigItem.of(HashMethod.MD5, "id"));

    public static void main(String[] args) throws Exception {
        FileSystem.init(null);

        String csv = "promoter-569.csv";
        File file = new File("D:\\data\\wefe\\" + csv);

        PsiECEncryptedData psiECEncryptedDataA = firstEncrypt(file);
        PsiECEncryptedData psiECEncryptedDataB = firstEncrypt(file);

        File secondEncryptedDataA = secondEncrypt(psiECEncryptedDataA.getDataFile(), psiECEncryptedDataB.ecdhPsiParam.secretKey);
        File secondEncryptedDataB = secondEncrypt(psiECEncryptedDataB.getDataFile(), psiECEncryptedDataA.ecdhPsiParam.secretKey);

        Set<ECPoint> pointsA = Files.readLines(secondEncryptedDataA, StandardCharsets.UTF_8)
                .stream()
                .map(x -> EllipticCurve.INSTANCE.base64ToECPoint(x))
                .collect(Collectors.toSet());

        Set<ECPoint> pointsB = Files.readLines(secondEncryptedDataB, StandardCharsets.UTF_8)
                .stream()
                .map(x -> EllipticCurve.INSTANCE.base64ToECPoint(x))
                .collect(Collectors.toSet());

        LongAdder count = new LongAdder();
        for (ECPoint point : pointsA) {
            if (pointsB.contains(point)) {
                count.increment();
            }
        }
        System.out.println("found:" + count);
    }

    private static File secondEncrypt(File file, BigInteger privateKey) throws IOException {
        File outputFile = FileSystem.getTempDir().resolve(UUID.randomUUID().toString() + ".data").toFile();
        try (BufferedWriter writer = FileUtil.buildBufferedWriter(outputFile, false)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    ECPoint point = EllipticCurve.INSTANCE.base64ToECPoint(line);
                    ECPoint encryptedValue = point.multiply(privateKey);
                    String output = PsiUtils.ecPoint2String(encryptedValue);
                    writer.write(output + System.lineSeparator());
                }
            }
        }
        return outputFile;
    }


    private static PsiECEncryptedData firstEncrypt(File file) throws Exception {
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file);

        try (PsiECEncryptedDataCreator creator = new PsiECEncryptedDataCreator(UUID.randomUUID().toString(), reader, hashConfig)) {
            return creator.create();
        }
    }
}
