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

import com.welab.wefe.common.Convert;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.io.excel.ExcelReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Used to read data set files in excel format
 *
 * @author zane.luo
 */
public class ExcelTableDataSourceReader extends AbstractTableDataSourceReader {
    private final ExcelReader reader;

    public ExcelTableDataSourceReader(File file) throws Exception {
        this(file, -1, -1);
    }

    public ExcelTableDataSourceReader(File file, long maxReadRows, long maxReadTimeInMs) throws Exception {
        super(maxReadRows, maxReadTimeInMs);
        reader = new ExcelReader(file);

        // 避免后续空指针
        getHeader();
    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        return reader.getColumnNames(0);
    }

    @Override
    public long doGetTotalDataRowCount() {
        return reader.getRowCount(0) - 1;
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {

        // Read data row
        Integer rowIndex = Convert.toInt(readDataRows + 1);
        List<Object> row = reader.getRowData(0, rowIndex, header.size());

        if (row == null) {
            return null;
        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < header.size(); i++) {

            // Supplement the default column of the data row to null
            Object value = row.size() > i ? row.get(i) : null;

            map.put(header.get(i), value);
        }
        return map;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
