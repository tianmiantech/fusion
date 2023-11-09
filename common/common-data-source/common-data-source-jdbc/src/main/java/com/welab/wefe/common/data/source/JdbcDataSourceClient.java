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

import com.welab.wefe.common.data_table.DataTable;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.CloseableUtils;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
public abstract class JdbcDataSourceClient<T extends JdbcDataSourceParams> extends AbstractDataSource<T> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public JdbcDataSourceClient(T params) {
        super(params);
    }


    public <I> void saveBatch(String sql, List<I> models, Function<I, Object[]> model2ArrayFunc) throws Exception {
        List<Object[]> list = new ArrayList<>();
        for (I item : models) {
            list.add(model2ArrayFunc.apply(item));
        }
        saveBatch(sql, list);
    }

    /**
     * 批量写入数据
     *
     * @param sql e.g: insert into table(id,name) values(?,?)
     */
    public void saveBatch(String sql, List<Object[]> rows) throws Exception {
        long start = System.currentTimeMillis();
        Connection conn = createConnection(true);
        conn.setAutoCommit(false);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            int count = 0;
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Object[] row = rows.get(rowIndex);
                for (int i = 0; i < row.length; i++) {
                    ps.setObject(i + 1, row[i]);
                }
                count++;
                ps.addBatch();
                if (rowIndex % 50000 == 0 && rowIndex > 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                    LOG.info("JdbcClient saveBatch count: " + count + ", rows size = " + rows.size());
                }
            }
            ps.executeBatch();
            conn.commit();
            ps.clearBatch();
        } catch (SQLException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        } finally {
            close(conn, ps, null);
            LOG.info("saveBatch spend：" + rows.size() + "rows " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    /**
     * @see {{@link #scan(String, BiConsumer, long, List)}}
     */
    public DataTable scan(String sql) throws Exception {
        DataTable table = new DataTable();

        scan(sql, (i, row) -> {
            if (table.getColumnCount() == 0) {
                table.addColumns(row.keySet());
            }
            table.addRow(row.values());
        });

        return table;
    }

    /**
     * @see {{@link #scan(String, BiConsumer, long, List)}}
     */
    public void scan(String sql, BiConsumer<Long, LinkedHashMap<String, Object>> consumer) throws Exception {
        scan(sql, consumer, 0);
    }

    /**
     * @see {{@link #scan(String, BiConsumer, long, List)}}
     */
    public void scan(String sql, BiConsumer<Long, LinkedHashMap<String, Object>> consumer, List<String> returnFields) throws Exception {
        scan(sql, consumer, 0, returnFields);
    }

    /**
     * @see {{@link #scan(String, BiConsumer, long, List)}}
     */
    public void scan(String sql, BiConsumer<Long, LinkedHashMap<String, Object>> consumer, long maxReadLine) throws Exception {
        scan(sql, consumer, maxReadLine, null);
    }

    /**
     * 执行查询，并流式读取。
     *
     * @param maxReadLine  最大读取行数，为 0 表示不指定。
     * @param returnFields 需要返回的字段列表，为空表示不指定。
     */
    public void scan(String sql, BiConsumer<Long, LinkedHashMap<String, Object>> consumer, long maxReadLine, List<String> returnFields) throws Exception {

        JdbcScanner scanner = createScanner(sql, maxReadLine, returnFields);
        long readRows = 0;
        try {
            while (true) {
                if (maxReadLine > 0 && readRows >= maxReadLine) {
                    break;
                }
                LinkedHashMap<String, Object> row = scanner.readOneRow();
                if (row == null) {
                    break;
                }

                consumer.accept(readRows, row);

                readRows++;
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            throw e;
        } finally {
            scanner.close();
        }
    }

    public JdbcScanner createScanner(String sql) throws Exception {
        return createScanner(sql, 0);
    }

    public JdbcScanner createScanner(String sql, long maxReadLine) throws Exception {
        LOG.info("create scanner by : " + sql);
        return createScanner(sql, maxReadLine, null);
    }

    /**
     * 创建 Scanner 对象，使用流式获取数据。
     *
     * @param sql          查询语句
     * @param maxReadLine  需要获取的最大数据量，默认为0，表示不指定。
     * @param returnFields 需要返回的字段列表，默认为 null，表示返回所有字段。
     */
    private JdbcScanner createScanner(String sql, long maxReadLine, List<String> returnFields) throws Exception {
        Connection conn = createConnection();
        return createScanner(conn, sql, maxReadLine, returnFields);
    }


    /**
     * 对于 hive，由于权限问题，有可能获取失败。
     */
    public long selectRowCount(String sql) throws Exception {
        sql = StringUtil.trim(sql, ' ', ';');

        PreparedStatement ps = null;
        ResultSet rs = null;
        long totalCount = 0;
        Connection conn = createConnection();
        try {
            ps = conn.prepareStatement("select count(*) from (" + sql + ") t");
            rs = ps.executeQuery();
            while (rs.next()) {
                totalCount = rs.getLong(1);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            close(conn, ps, rs);
        }

        return totalCount;
    }

    /**
     * Get the column header name of the query sql data
     */
    public List<String> getHeaders(String sql) throws Exception {
        List<String> result = new ArrayList<>();
        scan(
                sql,
                (i, row) -> {
                    if (result.isEmpty()) {
                        result.addAll(row.keySet());
                    }
                },
                1
        );

        return result;
    }

    /**
     * @see {{@link #queryList(String, List)}}
     */
    public List<Map<String, Object>> queryList(String sql) throws Exception {
        return queryList(sql, null);
    }

    /**
     * 执行查询，并获取全量的查询结果。
     *
     * @param returnFields 指定需要返回的字段列表
     */
    public List<Map<String, Object>> queryList(String sql, List<String> returnFields) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        scan(sql, (i, row) -> list.add(row), returnFields);
        return list;
    }

    /**
     * @see {{@link #queryOne(String, List)}}
     */
    public Map<String, Object> queryOne(String sql) throws Exception {
        return queryOne(sql, null);
    }

    /**
     * 执行查询，并获取第一条查询结果。
     *
     * @param returnFields 指定需要返回的字段列表
     */
    public Map<String, Object> queryOne(String sql, List<String> returnFields) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        scan(sql, (i, row) -> list.add(row), 1, returnFields);

        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * DROP TABLE IF EXISTS
     */
    public void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS `" + tableName + "`;";
        try {
            execute(sql);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定表的字段列表
     */
    public List<String> listTableFields(String tableName) throws Exception {
        return getHeaders("select * from `" + tableName + "` limit 1");
    }


    /**
     * 检查连接是否可用
     */
    public String test() throws StatusCodeWithException {
        return testSql("select 1");
    }

    /**
     * 检查 sql 是否正确
     */
    public String testSql(String sql) {
        try {
            execute(sql);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return e.getClass().getSimpleName() + ":" + e.getMessage();
        }
        return null;
    }

    public boolean execute(String sql) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = createConnection();
        try {
            ps = connection.prepareStatement(sql);
            // 务必加上这几个设置，否则默认取全量数据内存会炸。
            ps.setFetchSize(1);
            ps.setMaxRows(1);
            return ps.execute();
        } catch (SQLException e) {
            LOG.error(
                    e.getClass().getSimpleName() + " "
                            + e.getMessage()
                            + System.lineSeparator()
                            + "[sql]:" + sql,
                    e
            );
            throw e;
        } finally {
            close(connection, ps, rs);
        }
    }

    /**
     * 获取当前数据库中的所有表
     */
    public List<String> listTables() throws Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> tables = new ArrayList<>();
        Connection connection = createConnection();
        try {

            ps = connection.prepareStatement("show tables");
            rs = ps.executeQuery();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }

        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return tables;
        } finally {
            close(connection, ps, rs);
        }
        return tables;
    }

    protected Connection createConnection() throws Exception {
        return createConnection(false);
    }

    // region abstract method

    /**
     * 创建连接
     */
    protected abstract Connection createConnection(boolean batchModel) throws Exception;

    /**
     * 创建 JdbcScanner
     */
    protected abstract JdbcScanner createScanner(Connection conn, String sql, long maxReadLine, List<String> returnFields) throws Exception;

    // endregion


    // region static method

    public static List<String> getHeaders(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        List<String> list = new ArrayList<>();

        for (int i = 1; i <= columnCount; i++) {
            String header = metaData.getColumnName(i);
            if (header.contains(".")) {
                header = header.split("\\.")[1];
            }
            list.add(header);
        }
        return list;
    }

    public static void close(Connection conn, Statement ps, ResultSet rs) {
        CloseableUtils.closeQuietly(rs);
        CloseableUtils.closeQuietly(ps);
        CloseableUtils.closeQuietly(conn);
    }

    // endregion


}
