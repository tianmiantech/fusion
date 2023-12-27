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
package com.welab.fusion.core.Job;

import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.core.data_source.AbstractTableDataSourceReader;
import com.welab.wefe.common.util.CloseableUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public abstract class AbstractJobMember implements Closeable {
    public String memberId;
    public String memberName;
    public DataResourceInfo dataResourceInfo;

    public AbstractTableDataSourceReader tableDataResourceReader;
    /**
     * 全量的原始数据文件，仅包含任务需要的字段，明文。
     */
    public File allOriginalData;

    /**
     * 交集部分的原始数据文件，仅包含任务需要的字段，明文。
     */
    public File intersectionOriginalData;

    public AbstractJobMember(String memberId, String memberName, DataResourceInfo dataResourceInfo) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.dataResourceInfo = dataResourceInfo;
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.closeQuietly(tableDataResourceReader);
    }

}
