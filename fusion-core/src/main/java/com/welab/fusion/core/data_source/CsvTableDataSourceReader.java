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

package com.welab.fusion.core.data_source;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Used to read data set files in csv format
 * <p>
 * https://github.com/osiegmar/FastCSV
 *
 * @author zane.luo
 */
public class CsvTableDataSourceReader extends AbstractTableDataSourceReader {

    private final CsvParser parser;
    private long totalRowCount;
    private final File file;

    public CsvTableDataSourceReader(File file) throws Exception {
        this(file, -1, -1);
    }

    public CsvTableDataSourceReader(File file, long maxReadRows, long maxReadTimeInMs) throws Exception {
        super(maxReadRows, maxReadTimeInMs);
        if (!file.isFile() || !file.exists()) {
            throw new RuntimeException("文件不存在：" + file.getAbsolutePath());
        }

        this.file = file;

        CsvReader reader = new CsvReader();
        reader.setContainsHeader(false);
        reader.setSkipEmptyRows(true);
        this.parser = reader.parse(file, StandardCharsets.UTF_8);

        // 主动调用一次，避免外部使用时只接 readRow() 功能导致第一行数据读到列头。
        getHeader();
    }

    @Override
    protected List<String> doGetHeader() throws Exception {

        CsvRow row = parser.nextRow();
        return row.getFields();
    }

    @Override
    public long doGetTotalDataRowCount() {

        // Get the number of file lines
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file))) {
            lineNumberReader.skip(Long.MAX_VALUE);
            // 计算行数时，不包含列头。
            totalRowCount = lineNumberReader.getLineNumber() - 1L;
        } catch (IOException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return 0;
        }

        // 如果最后一行是空行，行数减一。
        if (totalRowCount > 0) {
            try (ReversedLinesFileReader reversedLinesReader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
                String lastLine = reversedLinesReader.readLine();
                if (StringUtil.isBlank(lastLine)) {
                    totalRowCount--;
                }
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
        }

        return totalRowCount;
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        try {
            CsvRow row = parser.nextRow();

            if (row == null) {
                return null;
            }

            for (int i = 0; i < getHeader().size(); i++) {
                String value = "";
                if (row.getFieldCount() > i) {
                    value = row.getField(i);
                }
                map.put(getHeader().get(i), value);
            }

        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "读取数据源中的第" + (readDataRows + 1) + "行失败：" + e.getMessage());
        }

        return map;
    }


    @Override
    public void close() throws IOException {
        parser.close();
    }
}
