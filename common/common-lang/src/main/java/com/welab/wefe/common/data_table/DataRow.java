package com.welab.wefe.common.data_table;

import com.alibaba.fastjson.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zane.luo
 * @date 2022/8/18 18:42
 */
public class DataRow {
    private DataCell[] cells;
    private DataTable table;

    public DataRow(DataTable table) {
        this(table, new Object[1]);
    }

    public DataRow(DataTable table, Collection<Object> values) {
        this(table, values.toArray());

    }

    public DataRow(DataTable table, Object... values) {
        this.table = table;

        clean();
        setCellsValue(values);
    }

    /**
     * 初始化行，并将这一行的所有 cell 中的值设置为 null。
     */
    public void clean() {
        if (cells == null) {
            cells = new DataCell[table.getColumnCount()];
        }

        for (int i = 0; i < cells.length; i++) {
            DataCell cell = cells[i];
            if (cell == null) {
                cells[i] = new DataCell(null);
            } else {
                cell.setValue(null);
            }

        }
    }

    public Map<String, Object> toMap() {
        List<DataColumn> columns = table.getColumns();
        Map<String, Object> map = new HashMap<>();
        for (DataColumn column : columns) {
            map.put(
                    column.getName(),
                    getCellValue(column)
            );
        }
        return map;
    }

    public JSONObject toJson() {
        return new JSONObject(toMap());
    }

    public <T> T toModel(Class<T> clazz) {
        return toJson().toJavaObject(clazz);
    }

    public void setCellValue(String columnName, Object value) {
        setCellValue(table.getColumnIndex(columnName), value);
    }

    public void setCellValue(int columnIndex, Object value) {
        cells[columnIndex] = new DataCell(value);
    }

    public void setCellsValue(Collection<Object> values) {
        setCellsValue(values.toArray());
    }

    public void setCellsValue(Object... values) {
        if (values == null) {
            clean();
            return;
        }

        for (int i = 0; i < values.length; i++) {
            if (i >= cells.length) {
                throw new RuntimeException("values count grant then table column count!");
            }
            DataCell cell = cells[i];
            if (cell == null) {
                cells[i] = new DataCell(values[i]);
            } else {
                cell.setValue(values[i]);
            }

        }
    }

    public DataCell[] getCells() {
        return cells;
    }

    public DataCell getCell(int index) {
        if (index >= cells.length) {
            return null;
        }
        return cells[index];
    }

    public DataTable getTable() {
        return table;
    }

    public <T> T getCellValue(DataColumn column) {
        return getCellValue(column.getIndex());
    }

    public <T> T getCellValue(int columnIndex) {
        DataCell cell = getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        return cell.getValue();
    }

    public Object[] getValues() {
        Object[] values = new Object[cells.length];
        for (int i = 0; i < cells.length; i++) {
            values[i] = cells[i].getValue();
        }
        return values;
    }
}
