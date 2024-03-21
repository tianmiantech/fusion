package com.welab.wefe.common.data_table;

import org.apache.commons.compress.utils.Sets;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author zane.luo
 * @date 2022/8/18 18:46
 */
public class DataColumn {
    private String dataType;
    private String name;
    private int index;

    public DataColumn() {
    }

    public DataColumn(String name) {
        this.name = name;
    }

    public DataColumn(String name, String dataType) {
        this.dataType = dataType;
        this.name = name;
    }

    private static final HashSet STRING_TYPE_NAMES = Sets.newHashSet(
            "varchar", "nvarchar", "text", "longtext"
    );

    private Class convertDataTypeFromDatabaseDataType(String databaseDataType) {
        String type = databaseDataType.toLowerCase();
        if (STRING_TYPE_NAMES.contains(type)) {
            return String.class;
        }
        return null;
    }

    void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataColumn that = (DataColumn) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    //region getter/setter

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    //endregion
}
