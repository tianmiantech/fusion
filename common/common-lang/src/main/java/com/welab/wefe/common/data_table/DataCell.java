package com.welab.wefe.common.data_table;

/**
 * @author zane.luo
 * @date 2022/8/18 18:42
 */
public class DataCell {
    private Object value;

    public DataCell(Object value) {
        this.value = value;
    }

    public <T> T getValue() {
        return (T) value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
