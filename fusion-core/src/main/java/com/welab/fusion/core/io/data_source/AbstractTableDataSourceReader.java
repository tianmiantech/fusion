/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.fusion.core.io.data_source;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.source.DorisDataSourceClient;
import com.welab.wefe.common.data.source.HiveDataSourceClient;
import com.welab.wefe.common.data.source.MySqlDataSourceClient;
import com.welab.wefe.common.data.source.SuperDataSourceClient;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * data set reader
 *
 * @author zane.luo
 */
public abstract class AbstractTableDataSourceReader implements Closeable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected List<String> header;
    /**
     * 总数据量
     */
    protected long totalDataRows = -1;
    /**
     * 已读取的数据量
     */
    protected long readDataRows = 0;
    /**
     * 最大读取行数
     */
    protected long maxReadRows;
    /**
     * 最大读取时长（毫秒）
     */
    protected long maxReadTimeInMs;
    /**
     * 是否已读取完毕
     */
    protected boolean finished = false;

    static {
        SuperDataSourceClient.register(DorisDataSourceClient.class);
        SuperDataSourceClient.register(HiveDataSourceClient.class);
        SuperDataSourceClient.register(MySqlDataSourceClient.class);
    }

    public AbstractTableDataSourceReader(long maxReadRows, long maxReadTimeInMs) throws StatusCodeWithException {
        this.maxReadRows = maxReadRows;
        this.maxReadTimeInMs = maxReadTimeInMs;

        // 避免后续空指针
        getHeader();
    }

    public synchronized long getTotalDataRowCount() {
        if (totalDataRows > 0) {
            return totalDataRows;
        }

        totalDataRows = doGetTotalDataRowCount();
        return totalDataRows;
    }

    public synchronized List<String> getHeader() throws StatusCodeWithException {
        if (header != null) {
            return header;
        }

        List<String> list = null;
        try {
            list = doGetHeader();
        } catch (Exception e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "读取数据集 header 信息失败：" + e.getMessage());
        }


        for (int i = 0; i < list.size(); i++) {
            String columnName = list.get(i);
            if (StringUtil.isEmpty(columnName)) {
                StatusCode.PARAMETER_VALUE_INVALID
                        .throwException("数据集列头中第" + (i + 1) + "列名称为空，请处理后重试。");
            }
        }

        if (list.stream().distinct().count() != list.size()) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "数据集包含重复的字段，请处理后重新上传。");
        }

        if (list.size() == 0) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "数据集首行为空");
        }

        header = list;

        return header;
    }

    /**
     * Read data row
     *
     * @param dataRowConsumer Data row consumption method
     */
    public void readRows(BiConsumer<Long, LinkedHashMap<String, Object>> dataRowConsumer) throws StatusCodeWithException {
        finished = false;
        long start = System.currentTimeMillis();

        LinkedHashMap<String, Object> row;
        long now = System.currentTimeMillis();
        while ((row = readOneRow()) != null) {

            if (getHeader().size() != row.size()) {
                finished = true;
                StatusCode
                        .PARAMETER_VALUE_INVALID
                        .throwException(
                                "数据集第" + readDataRows + "行有" + row.size()
                                        + "列，与列头数（" + getHeader().size()
                                        + "）不匹配，请处理后重新上传。"
                        );
            }

            dataRowConsumer.accept(readDataRows, row);

            readDataRows++;
            if (readDataRows % 500000 == 0) {
                LOG.info("read 50w duration " + (System.currentTimeMillis() - now) + ", readDataRows = " + readDataRows);
                now = System.currentTimeMillis();
            }
            // Limit the number of rows read
            if (maxReadRows > 0 && readDataRows >= maxReadRows) {
                finished = true;
                break;
            }

            // Limit the duration of reading
            if (maxReadTimeInMs > 0 && System.currentTimeMillis() - start > maxReadTimeInMs) {
                finished = true;
                break;
            }
        }

        finished = true;
    }

    public long getReadDataRows() {
        return readDataRows;
    }


    protected abstract List<String> doGetHeader() throws Exception;

    /**
     * 获取数据源中的全量数据行数
     */
    protected abstract long doGetTotalDataRowCount();

    /**
     * Read data row
     */
    protected abstract LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException;

    public boolean isFinished() {
        return finished;
    }
}
