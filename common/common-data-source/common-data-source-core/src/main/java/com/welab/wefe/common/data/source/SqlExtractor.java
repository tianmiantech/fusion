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

import com.welab.wefe.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/4/18
 */
public class SqlExtractor {
    private List<String> result = new ArrayList<>();

    /**
     * 正在拼接的 sql
     */
    StringBuilder sqlBuilder = new StringBuilder(128);

    /**
     * 结束一条sql语句的捕获，并准备捕获下一条。
     */
    private void finishOneSql() {
        String sql = sqlBuilder.toString().trim();
        if (StringUtil.isNotEmpty(sql)) {
            result.add(sql);
        }
        sqlBuilder = new StringBuilder(128);
    }

    /**
     * 从文本中抽取出所有 sql
     */
    public List<String> extract(String text) {
        text = text.trim();

        if (StringUtil.isEmpty(text)) {
            return result;
        }

        String[] lines = text.split("\n");


        boolean inMultiLineComment = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("--") || line.startsWith("#")) {
                finishOneSql();
                continue;
            }
            if (line.startsWith("/*")) {
                finishOneSql();
                inMultiLineComment = true;
                continue;
            }
            if (line.endsWith("*/")) {
                inMultiLineComment = false;
                continue;
            }
            if (inMultiLineComment || line.isEmpty()) {
                continue;
            }

            sqlBuilder
                    .append(line)
                    .append(System.lineSeparator());

            if (line.endsWith(";")) {
                finishOneSql();
            }
        }

        finishOneSql();
        return result;
    }
}
