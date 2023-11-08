package com.welab.wefe.common.data_table;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 一个离线的二维表
 *
 * @author zane.luo
 * @date 2022/8/18 18:42
 */
public class DataTable {
    private String name;
    private DataColumnSet columnSet = new DataColumnSet();
    private List<DataRow> rows = new ArrayList<>();

    private SqlBuilder sqlBuilder;

    public DataTable() {
    }

    public DataTable(String name) {
        this.name = name;
    }

    public DataTable(String name, String... columns) {
        this.name = name;
        for (String column : columns) {
            addColumn(column);
        }
    }

    /**
     * 读取 ResultSet，并将结果储存在 DataTable 中。
     */
    public static DataTable fromMetaData(ResultSetMetaData rsMeta) throws SQLException {
        DataTable table = new DataTable();
        // 读取元数据，填充 TableColumn。
        int columnCount = rsMeta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            table.addColumn(rsMeta.getColumnName(i), rsMeta.getColumnTypeName(i));
        }
        return table;
    }

    /**
     * 读取 ResultSet，并将结果储存在 DataTable 中。
     */
    public static DataTable fromSqlResultSet(ResultSet resultSet) throws SQLException {
        DataTable table = fromMetaData(resultSet.getMetaData());


        // 遍历读取数据行
        while (resultSet.next()) {
            DataRow row = table.readRowFromResultSet(resultSet);
            table.addRow(row);
        }
        return table;
    }

//    /**
//     * 读取 ResultSet，并将数据提供给消费者。
//     *
//     * @param forceStringModel 是否强制使用 String 模式
//     *                         由于某些情况下无法从元数据中获取正确的数据类型，采用此模式可以忽略这些问题。
//     */
//    public void loadRows2BatchConsumer(boolean forceStringModel,BatchConsumer<DataRow>  batchConsumer) throws SQLException {
//
//        // 遍历读取数据行
//        while (resultSet.next()) {
//            DataRow row = readRowFromResultSet(forceStringModel);
//            batchConsumer.add(row);
//        }
//        batchConsumer.waitForFinishAndClose();
//    }

//    /**
//     * 读取 ResultSet，并将结果储存在 DataTable 中。
//     *
//     * @param forceStringModel 是否强制使用 String 模式
//     *                         由于某些情况下无法从元数据中获取正确的数据类型，采用此模式可以忽略这些问题。
//     */
//    public DataTable loadRows2Memory(boolean forceStringModel) throws SQLException {
//
//
//    }


    private DataRow readRowFromResultSet(ResultSet resultSet) throws SQLException {
        DataRow row = new DataRow(this);

        for (int i = 0; i < getColumnCount(); i++) {
            // 这里起点为 1，不是 0。
            Object value = resultSet.getObject(i + 1);
            row.setCellValue(i, value);
        }

        return row;
    }

    private DataRow readRowFromResultSet(ResultSet resultSet, boolean forceStringModel) throws SQLException {
        DataRow row = new DataRow(this);

        for (int i = 0; i < getColumnCount(); i++) {
            // 这里起点为 1，不是 0。
            Object value = resultSet.getObject(i + 1);

            // 如果是 forceStringModel 模式，使用 String 接收结果。
            if (value != null && forceStringModel && !(value instanceof String)) {
                if (value instanceof byte[]) {
                    value = new String((byte[]) value, StandardCharsets.UTF_8);
                } else {
                    value = String.valueOf(value);
                }
            }

            row.setCellValue(i, value);
        }

        return row;
    }


    // region add

    private DataTable addRow(DataRow row) {
        rows.add(row);
        return this;
    }

    public DataTable addRow(Collection<Object> values) {
        rows.add(new DataRow(this, values));
        return this;
    }

    public DataTable addRow(Object... values) {
        rows.add(new DataRow(this, values));
        return this;
    }

    public DataTable addColumn(String columnName) {
        columnSet.add(new DataColumn(columnName));
        return this;
    }

    public DataTable addColumn(String columnName, String dataType) {
        columnSet.add(new DataColumn(columnName, dataType));
        return this;
    }

    public void addColumns(Collection<String> names) {
        names.forEach(this::addColumn);
    }
    // endregion


    // region get

    public int getColumnCount() {
        return columnSet.size();
    }

    public int getColumnIndex(String columnName) {
        return columnSet.getColumnIndex(columnName);
    }

    public DataRow getRow(int index) {
        return rows.get(index);
    }

    public final List<DataRow> getRows() {
        return rows;
    }

    /**
     * 获取指定列所有值
     */
    public <T> List<T> getColumnValues(int columnIndex) {
        List<T> list = new ArrayList<>(rows.size());
        for (DataRow row : rows) {
            list.add(row.getCellValue(columnIndex));
        }
        return list;
    }

    public final List<DataColumn> getColumns() {
        return columnSet.getColumns();
    }

    // endregion


    // region to

    public <T> List<T> toModels(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (DataRow row : rows) {
            list.add(row.toModel(clazz));
        }
        return list;
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();
        root.put("columns", columnSet.getColumnNames());
        root.put(
                "rows",
                rows
                        .stream()
                        .map(x -> x.getValues())
                        .toArray()
        );
        return root;
    }

    // endregion

    public void print() {
        if (StringUtils.isNotEmpty(name)) {
            System.out.println("------------------------------ " + name + " ------------------------------");
        }
        for (DataColumn column : getColumns()) {
            System.out.print("|" + column.getName() + "\t");
        }
        System.out.print("|");
        System.out.println();

        for (DataRow row : rows) {
            for (DataCell cell : row.getCells()) {
                System.out.print("|" + cell.getValue() + "\t");
            }
            System.out.print("|");
            System.out.println();
        }
    }

    public SqlBuilder getSqlBuilder() {
        if (sqlBuilder == null) {
            sqlBuilder = new SqlBuilder(this);
        }
        return sqlBuilder;
    }

}
