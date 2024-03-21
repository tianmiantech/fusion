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

import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.exception.StatusCodeWithException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zane
 * @date 2023/8/16
 */
public class DorisScanner extends MysqlScanner {
    /**
     * 查询超时时间
     */
    private static final long QUERY_TIMEOUT = TimeSpan.fromDays(1).toSeconds();

    public DorisScanner(Connection conn, String sql, long maxReadLine) throws SQLException, StatusCodeWithException {
        super(conn, sql, maxReadLine);
    }

    public DorisScanner(Connection conn, String sql, long maxReadLine, List<String> returnFields) throws SQLException, StatusCodeWithException {
        super(conn, sql, maxReadLine, returnFields);
    }


    @Override
    protected ResultSet execute() throws SQLException {
        /**
         * doris 默认的查询超时时间是五分钟，当数据量大的时候会超时，这里指定一下超时时间避免超时。
         * 考虑到权限问题，不一定能设置 global 级别的参数，所以在 session 级别设置。
         */
        conn.prepareStatement("SET query_timeout = " + QUERY_TIMEOUT + ";").execute();

        boolean showQuery = sql.trim().toLowerCase().startsWith("show ");
        /**
         * doris 指定 statement.setLargeMaxRows(maxReadLine) 不生效，只好封装成子查询。
         *
         * 如果不限定最大读取量，在 getHeader()、数据预览 等不需要读全部数据的场景会超时。
         * 因为驱动会读取全表数据，在表数据量大时非常耗时。
         */
        if (!showQuery) {
            sql = maxReadLine > 0
                    ? "select * from (" + sql + ") as a limit " + maxReadLine
                    : sql;
        }

        return super.execute();
    }
}
