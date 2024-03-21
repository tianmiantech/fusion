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
package com.welab.fusion.core.Job.base;

/**
 * 不管是什么 pis 算法，最终都需要在其中一个节点上进行最后的求交动作，这个节点就是 leader。
 *
 * @author zane.luo
 * @date 2023/11/13
 */
public enum JobRole {
    /**
     * 首领
     */
    leader,
    /**
     * 从属
     */
    follower,

}
