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
package com.welab.wefe.common.tuple;

/**
 * @author zane
 * @date 2023/7/19
 */
public final class Tuple2<T1, T2> {
    private final T1 value1;
    private final T2 value2;

    public static <T1, T2> Tuple2<T1, T2> of(T1 value1, T2 value2) {
        return new Tuple2<>(value1, value2);
    }

    public Tuple2(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T1 getValue1() {
        return this.value1;
    }

    public T2 getValue2() {
        return this.value2;
    }


    @Override
    public int hashCode() {
        int result = 0;
        result *= 31;
        if (value1 != null) {
            result += value1.hashCode();
        }

        result *= 31;
        if (value2 != null) {
            result += value2.hashCode();
        }

        return result;
    }

    @Override
    public String toString() {
        return "Tuple3{value1=" + this.value1 + ", value2=" + this.value2 + "}";
    }
}
