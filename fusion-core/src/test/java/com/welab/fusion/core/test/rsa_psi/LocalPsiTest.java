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
package com.welab.fusion.core.test.rsa_psi;

import cn.hutool.core.codec.Base64;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.psi.PsiRecord;
import com.welab.fusion.core.psi.PsiUtils;
import com.welab.wefe.common.BatchConsumer;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
public class LocalPsiTest {

    private static HashConfig hashConfig = HashConfig.of(HashConfigItem.of(HashMethod.MD5, "id"));

    public static void main(String[] args) throws Exception {
        String csv = "promoter-569.csv";
        File file = new File("D:\\data\\wefe\\" + csv);

        PsiBloomFilter psiBloomFilter = createPsiBloomFilter(file);

        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file);
        BigInteger publicModulus = psiBloomFilter.rsaPsiParam.publicModulus;
        BigInteger publicExponent = psiBloomFilter.rsaPsiParam.publicExponent;

        LongAdder progress = new LongAdder();
        LongAdder fruitCount = new LongAdder();
        File resultFile = new File("");

        // 批处理
        BatchConsumer<PsiRecord> consumer = new BatchConsumer<>(10, 5_000, records -> {
            try {
                // 将数据发送到过滤器方加密
                List<String> encryptedList = PsiUtils.encryptPsiRecords(
                        psiBloomFilter,
                        records.stream().map(x -> x.encodedKey).collect(Collectors.toList())
                );

                List<PsiRecord> fruit = PsiUtils.match(
                        psiBloomFilter,
                        records,
                        encryptedList,
                        publicModulus
                );


                System.out.println("fruit size: " + fruit.size());
                // 更新进度
                fruitCount.add(fruit.size());
                progress.add(records.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 从数据源逐条读取数据集并编码
        reader.readRows((index, row) -> {

            PsiRecord record = new PsiRecord();
            record.row = row;

            BigInteger blindFactor = PsiUtils.generateBlindingFactor(publicModulus);
            BigInteger random = blindFactor.modPow(publicExponent, publicModulus);
            record.inv = blindFactor.modInverse(publicModulus);

            String key = hashConfig.hash(row);
            BigInteger h = PsiUtils.stringToBigInteger(key);
            BigInteger x = h.multiply(random).mod(publicModulus);
            byte[] bytes = PsiUtils.bigIntegerToBytes(x);
            record.encodedKey = Base64.encode(bytes);

            consumer.add(record);
        });

        // 等待消费完成
        consumer.waitForFinishAndClose();
    }

    private static PsiBloomFilter createPsiBloomFilter(File file) throws Exception {
        CsvTableDataSourceReader reader = new CsvTableDataSourceReader(file);

        // 生成过滤器
        try (PsiBloomFilterCreator creator = new PsiBloomFilterCreator("test", reader, hashConfig)) {
            return creator.create();
        }
    }
}
