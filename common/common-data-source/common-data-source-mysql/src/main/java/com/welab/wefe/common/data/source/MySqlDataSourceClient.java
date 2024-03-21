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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
@DataSource(typeNames = {"mysql"})
public class MySqlDataSourceClient extends JdbcDataSourceClient<JdbcDataSourceParams> {
    public MySqlDataSourceClient(String host, Integer port, String userName, String password, String dbName) {
        super(
                JdbcDataSourceParams.of(
                        host,
                        port,
                        userName,
                        password,
                        dbName
                )
        );
    }

    public MySqlDataSourceClient(JdbcDataSourceParams params) {
        super(params);
    }

    @Override
    protected Connection createConnection(boolean batchModel) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String url = String.format(
                "jdbc:mysql://%s:%d/%s?characterEncoding=UTF-8&useSSL=false&useUnicode=true&serverTimezone=Asia/Shanghai"
                        +
                        (
                                batchModel
                                        ? "&rewriteBatchedStatements=true"
                                        : ""
                        )
                , params.host, params.port, params.databaseName);
        return DriverManager.getConnection(url, params.userName, params.password);

    }

    @Override
    protected JdbcScanner createScanner(Connection conn, String sql, long maxReadLine, List<String> returnFields) throws Exception {
        return new MysqlScanner(conn, sql, maxReadLine, returnFields);
    }
}
