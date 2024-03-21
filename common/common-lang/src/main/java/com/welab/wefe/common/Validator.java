/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common;

import com.welab.wefe.common.exception.StatusCodeWithException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Used for various calibrations
 *
 * @author zane.luo
 */
public class Validator {

    private static final Pattern MATCH_UNSIGNED_INTEGER_PATTERN = Pattern.compile("^\\d+$");
    private final static Pattern MATCH_BOOLEAN_PATTERN = Pattern.compile("^true$|^false$|^0$|^1$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOT_SELECT_SYNTAX;

    static {
        List<String> keywords = Arrays.asList("insert", "into", "update", "delete", "truncate", "drop", "create");
        String regex = keywords.stream()
                .map(x -> "\\b" + x + "\\b")
                .collect(Collectors.joining("|"));
        NOT_SELECT_SYNTAX = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public static boolean isBoolean(Object value) {
        if (value instanceof Boolean) {
            return true;
        }
        return MATCH_BOOLEAN_PATTERN.matcher(value.toString()).find();
    }

    public static boolean isLong(Object value) {
        if (value instanceof Long) {
            return true;
        }
        String str = value.toString();

        try {
            Long.valueOf(str);
        } catch (NumberFormatException e) {
            // 科学计数法的数字会被识别为 double，所以先转为非科学计数法之后再判断是否是 long。
            if (str.contains("E") || str.contains("e")) {
                try {
                    Double aDouble = Double.valueOf(value.toString());
                    BigDecimal bigDecimal = BigDecimal.valueOf(aDouble);
                    return isLong(bigDecimal.toPlainString());
                } catch (NumberFormatException e2) {
                    return false;
                }
            }

            return false;
        }
        return true;
    }

    public static boolean isDouble(Object value) {
        if (value instanceof Double) {
            return true;
        }
        try {
            Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    public static boolean isInteger(Object value) {
        if (value instanceof Integer) {
            return true;
        }
        try {
            Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    /**
     * Is it an unsigned integer
     */
    public static boolean isUnsignedInteger(Object value) {

        if (value instanceof Integer) {
            return (Integer) value >= 0;
        }

        return MATCH_UNSIGNED_INTEGER_PATTERN.matcher(String.valueOf(value)).find();
    }

    /**
     * 判断一个字符串是否是 DateTime 格式
     */
    public static boolean isDateTime(String value) {
        try {
            return Convert.toDate(value) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static void mustBeMultiSelectSql(String text) throws StatusCodeWithException {
        List<String> sqlList = Extractor.extractSqlList(text);

        for (String sql : sqlList) {
            if (!isSelectSql(sql)) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("仅支持 select 查询！");
            }
        }
    }

    public static void mustBeOneSelectSql(String text) throws StatusCodeWithException {
        List<String> sqlList = Extractor.extractSqlList(text);
        if (sqlList.size() > 1) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("仅支持查询一条 SQL 语句，当前输入了 " + sqlList.size() + " 条 SQL。");
        }

        for (String sql : sqlList) {
            if (!isSelectSql(sql)) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("仅支持 select 查询！");
            }
        }
    }

    public static boolean isSelectSql(String sql) {
        sql = sql.trim().toLowerCase();
        return sql.startsWith("select ") && !NOT_SELECT_SYNTAX.matcher(sql).find();
    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList(
                "select * from table",
                "SELECT * INTO newtable [IN externaldb] FROM table1;",
                "SELECT column_name(s) INTO newtable [IN externaldb] FROM table1;",
                "select * from table delete",
                "select * from table drop",
                "select deleted from table"
        );
        for (String sql : list) {
            System.out.println(isSelectSql(sql) + ": " + sql);
        }

    }
}
