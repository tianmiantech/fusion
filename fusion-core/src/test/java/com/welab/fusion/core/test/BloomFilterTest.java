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
package com.welab.fusion.core.test;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * @author zane.luo
 * @date 2023/12/13
 */
public class BloomFilterTest {
    public static void main(String[] args) {
        // 在预估样本为 100，fpp 为 0.0001 时，实际生成的过滤器数组长度为 1917。
        int expectedInsertions = 100;
        double fpp = 0.0001;
        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(Charsets.UTF_8),
                expectedInsertions,
                fpp
        );

        // 写入 A 方 Id
        for (int i = 0; i < expectedInsertions; i++) {
            bloomFilter.put(i + "");
        }

        // 使用 B 方 Id 进行碰撞
        int falsePositive = 0;
        int matchIdCount = 99;
        for (int i = expectedInsertions; i < matchIdCount; i++) {
            boolean contains = bloomFilter.mightContain(i + "");
            if (contains) {
                falsePositive++;
                System.out.println("误判：" + i);
            }
        }

        System.out.println("【过滤器方】");
        System.out.println("过滤器初始化参数：预计数据量 " + expectedInsertions + " 假阳性概率 0.0001");
        System.out.println("过滤器实际长度：" + optimalNumOfBits(expectedInsertions, fpp));
        System.out.println("过滤器写入数据量：" + expectedInsertions);
        System.out.println();
        System.out.println("【数据集方】");
        System.out.println("数据量：" + matchIdCount);
        System.out.println("误判次数：" + falsePositive);
    }

    /**
     * 计算 bit 数组长度
     *
     * @param expectedInsertions 预估写入数据量
     * @param fpp                期望的误判率
     */
    static long optimalNumOfBits(long expectedInsertions, double fpp) {
        if (fpp == 0) {
            fpp = Double.MIN_VALUE;
        }
        return (long) (-expectedInsertions * Math.log(fpp) / (Math.log(2) * Math.log(2)));
    }
}
