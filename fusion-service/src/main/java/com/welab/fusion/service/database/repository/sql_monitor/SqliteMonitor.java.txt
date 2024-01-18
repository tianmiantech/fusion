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
package com.welab.fusion.service.database.repository.sql_monitor;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;

import java.util.*;

/**
 * sql 监视器
 *
 * @author zane
 * @date 2022/3/22
 */
public class SqliteMonitor extends FilterEventAdapter {
    private SQLUtils.FormatOption statementSqlFormatOption = new SQLUtils.FormatOption(false, true);
    public static final Set<String> synSet = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        in(statement, sql);
    }


    @Override
    protected void statementExecuteBatchBefore(StatementProxy statement) {
        in(statement, statement.getBatchSql());
    }

    @Override
    protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
        in(statement, sql);
    }

    @Override
    protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
        in(statement, sql);
    }


    /**
     * 在 statement.execute() 之后被调用
     */
    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
        out(statement, sql);
    }

    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
        out(statement, sql);
    }

    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        out(statement, sql);
    }

    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        out(statement, statement.getBatchSql());
    }


    @Override
    protected void statement_executeErrorAfter(StatementProxy statement, String sql, Throwable error) {
        out(statement, sql);
    }

    private void in(StatementProxy statement, String sql) {
        synSet.add(
                renderSql(statement, sql)
        );
    }

    private void out(StatementProxy statement, String sql) {
        synSet.remove(
                renderSql(statement, sql)
        );
    }

    private String renderSql(StatementProxy statement, String sql) {
        int parametersSize = statement.getParametersSize();
        if (parametersSize > 0) {
            List<Object> parameters = new ArrayList<Object>(parametersSize);
            for (int i = 0; i < parametersSize; ++i) {
                JdbcParameter jdbcParam = statement.getParameter(i);
                parameters.add(jdbcParam != null
                        ? jdbcParam.getValue()
                        : null);
            }
            String dbType = statement.getConnectionProxy().getDirectDataSource().getDbType();
            sql = SQLUtils.format(sql, DbType.valueOf(dbType), parameters, this.statementSqlFormatOption);

        }

        return sql;
    }

}
