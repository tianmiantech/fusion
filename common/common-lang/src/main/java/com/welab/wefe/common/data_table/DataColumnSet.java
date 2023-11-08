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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataColumn 的集合
 *
 * @author zane
 * @date 2022/8/22
 */
class DataColumnSet {
    private Set<DataColumn> set = new HashSet<>();
    private List<DataColumn> list = new ArrayList<>();

    public synchronized void add(DataColumn dataColumn) {
        if (set.contains(dataColumn)) {
            return;
        }

        dataColumn.setIndex(set.size());

        set.add(dataColumn);
        list.add(dataColumn);
    }

    public final List<DataColumn> getColumns() {
        return list;
    }

    public int getColumnIndex(String columnName) {
        for (DataColumn column : list) {
            if (column.getName().equals(columnName)) {
                return column.getIndex();
            }
        }

        return -1;
    }

    public int size() {
        return list.size();
    }

    public List<String> getColumnNames() {
        return list
                .stream()
                .map(x -> x.getName())
                .collect(Collectors.toList());
    }
}
