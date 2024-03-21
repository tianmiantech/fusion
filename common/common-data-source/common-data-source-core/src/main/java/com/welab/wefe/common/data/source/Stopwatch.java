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
package com.welab.wefe.common.data.source;

import java.io.Closeable;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
public class Stopwatch implements Closeable {
    private String name;
    private long start = System.nanoTime();

    public Stopwatch(String name) {
        this.name = name;
    }

    @Override
    public void close() {
        long spend = (System.nanoTime() - start) / 1000;
        if (spend < 1000) {
            System.out.println(name + " " + spend + "微秒");
            return;
        }
        spend /= 1024;
        if (spend < 1000) {
            System.out.println(name + " " + spend + "毫秒");
            return;
        }
        spend /= 1024;
        if (spend < 1000) {
            System.out.println(name + " " + spend + "秒");
            return;
        }

    }

}
