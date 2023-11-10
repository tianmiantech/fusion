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
import com.welab.wefe.common.data.source.JdbcDataSourceClient;
import com.welab.wefe.common.data.source.JdbcScanner;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Used to read data sets in Sql format
 *
 * @author jacky.jiang
 */
public class SqlTableDataSourceReader extends AbstractTableDataSourceReader {

    private final JdbcDataSourceClient<?> jdbcClient;
    private String sql;
    private JdbcScanner scanner;

    public SqlTableDataSourceReader(JdbcDataSourceClient<?> jdbcClient, String sql) throws Exception {
        this.jdbcClient = jdbcClient;
        this.sql = sql;
        this.scanner = jdbcClient.createScanner(sql);

    }

    @Override
    protected List<String> doGetHeader() throws Exception {
        if (!CollectionUtils.isEmpty(this.header)) {
            return this.header;
        }

        this.header = jdbcClient.getHeaders(sql);
        return this.header;
    }

    @Override
    public long doGetTotalDataRowCount() {

        try {
            return jdbcClient.selectRowCount(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected LinkedHashMap<String, Object> readOneRow() throws StatusCodeWithException {
        try {
            return scanner.readOneRow();
        } catch (Exception e) {
            StatusCode.SQL_ERROR.throwException(e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }


    @Override
    public void close() throws IOException {
        if (scanner != null) {
            scanner.close();
        }
    }

    public String getSql() {
        return sql;
    }
}
