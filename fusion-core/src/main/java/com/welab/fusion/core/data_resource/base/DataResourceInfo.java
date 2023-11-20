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
package com.welab.fusion.core.data_resource.base;

import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public class DataResourceInfo {
    public String id;
    public String name;
    public long dataCount;
    public DataResourceType dataResourceType;
    public HashConfig hashConfig;

    public static DataResourceInfo of(String id,String name,long dataCount,DataResourceType dataResourceType,HashConfig hashConfig){
        DataResourceInfo dataResourceInfo = new DataResourceInfo();
        dataResourceInfo.id = id;
        dataResourceInfo.name = name;
        dataResourceInfo.dataCount = dataCount;
        dataResourceInfo.dataResourceType = dataResourceType;
        dataResourceInfo.hashConfig = hashConfig;
        return dataResourceInfo;
    }
}