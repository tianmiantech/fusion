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
package com.welab.wefe.common.data_table;

import com.welab.wefe.common.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2022/12/14
 */
public class SqlBuilder {
    private DataTable dataTable;

    public SqlBuilder(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public static String buildDropTableSql(String tableName) {
        return "DROP TABLE IF EXISTS `" + tableName + "`;";
    }

    /**
     * 生成建表语句，用于建表储存 presto 查询结果。
     *
     * 这里生成的建表语句为 presto 查询结果专用
     * 对所有字段强制使用 text 类型进行存储，增加通用适配能力，避免类型映射不支持导致的数据存储失败。
     *
     * 专用的原因：
     * 1. VARCHAR(65535)，presto 返回的查询结果中字段类型声明的字段长度超过了 mysql 允许的上限。
     * 2. VARBINARY， mysql 不支持的数据类型。
     * 3. TIMESTAMP， 该数据类型 mysql 不允许为空，但返回值可能为空，导致结果无法写入。
     */
    public String buildCreateTableSqlForPrestoQueryResult(String tableName, String tableComment) {
        List<String> columnLines = dataTable.getColumns()
                .stream()
                .map(x -> {
                    return "`" + x.getName() + "` " + " TEXT DEFAULT NULL";
                })
                .collect(Collectors.toList());

        // 对单引号做转义，避免语法结构被破坏。
        tableComment = tableComment.replace('\'', '"');

        return "CREATE TABLE `" + tableName + "`\n" +
                "(\n" +
                StringUtil.join(columnLines, "," + System.lineSeparator()) +
                "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='" + tableComment + "';";
    }

    /**
     * 注意：暂未测试 mysql 以外的数据库类型是否适用此方法。
     */
    public String buildCreateTableSql(String tableName, String tableComment) {
        List<String> columnLines = dataTable.getColumns()
                .stream()
                .map(x -> {
                    String dataType = x.getDataType().toUpperCase();
                    /**
                     * 这里读到的类型有可能是 “VARCHAR(65535)”
                     * 这超出了 varchar 允许的长度
                     * 所以统一使用 text 类型避免异常。
                     *
                     */
                    if (dataType.contains("VARCHAR")) {
                        dataType = "text";
                    } else if (dataType.contains("VARBINARY")) {
                        dataType = "text";
                    }
                    return "`" + x.getName() + "` " + dataType + " DEFAULT NULL";
                })
                .collect(Collectors.toList());

        // 对单引号做转义，避免语法结构被破坏。
        tableComment = tableComment.replace('\'', '"');

        return "CREATE TABLE `" + tableName + "`\n" +
                "(\n" +
                StringUtil.join(columnLines, "," + System.lineSeparator()) +
                "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='" + tableComment + "';";
    }

    public String buildInsertSql(String tableName) {
        List<String> columnNames = dataTable.getColumns()
                .stream()
                .map(x -> "`" + x.getName() + "`")
                .collect(Collectors.toList());

        List<String> columnValues = dataTable.getColumns()
                .stream()
                .map(x -> "?")
                .collect(Collectors.toList());

        return "insert into `" + tableName + "`(\n" +
                StringUtil.join(columnNames, ",") +
                "\n)" +
                "values(\n" +
                StringUtil.join(columnValues, ", ") +
                "\n)";
    }
}
