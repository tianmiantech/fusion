package com.welab.wefe.common;

import com.welab.wefe.common.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 助手类，用于从字符串中抽取各种基础数据。
 *
 * @author Zane
 */
public class Extractor {

    private static final Pattern PATTERN_MATCH_NUMBER = Pattern.compile("(?<num>-?((\\d{1,3},)(\\d{3},)*\\d{3}|\\d+)(\\.\\d+)?) *(?<unit>[kK%千万]?)", Pattern.CASE_INSENSITIVE);
    private static final String ADDRESS_CODE = "(11|12|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82)";
    private static final Pattern PATTERN_MATCH_CNDI = Pattern.compile("(?<cnid>(" + ADDRESS_CODE + "\\d{4}(((18|19|20)\\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx])|((\\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\d{2}[0-9Xx]))))?)(?<cnidWithMask>(" + ADDRESS_CODE + "\\d{4}(((18|19|20)\\d{2}((\\*{4}\\d{3}[0-9Xx])|(((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\*{4})))|((\\d{2}((\\*{4}\\d{2}[0-9Xx])|(((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\*{3}))))))?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_MATCH_ALL_NUMBER = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);


    /**
     * 从字符串中抽取出价格,如果没抽到，返回null。
     *
     * @param str
     * @return
     */
    public static BigDecimal extractPrice(String str) {
        List<BigDecimal> result = extractNumbers(str);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }


    /**
     * 从字符串中抽取出double,如果没抽到，返回null。
     *
     * @param str
     * @return
     */
    public static BigDecimal extractDouble(String str) {
        List<BigDecimal> result = extractNumbers(str);
        if (result.size() > 0) {
            return result.get(0).abs();
        }
        return null;
    }


    /**
     * 从字符串中抽取出数字
     * <remark>
     * * 此方法会自动识别 k、千、万 等单位
     * * 此方法会自动处理 三位分节法
     * </remark>
     *
     * @param str
     * @return
     */
    public static List<BigDecimal> extractNumbers(String str) {

        List<BigDecimal> result = new ArrayList<>();

        Matcher matcher = PATTERN_MATCH_NUMBER.matcher(str);
        while (matcher.find()) {
            String numStr = matcher.group("num").replace(",", "");
            numStr = StringUtil.ToDbc(numStr);
            BigDecimal num = BigDecimal.valueOf(Convert.toDouble(numStr));
//            BigDecimal value = BigDecimal.valueOf(num);
            switch (matcher.group("unit").toLowerCase()) {
                case "k":
                case "千":
                    num = num.multiply(new BigDecimal(1000));
//                    num *= 1000;
                    break;
                case "万":
                    num = num.multiply(new BigDecimal(10000));
//                    num *= 10000;
                    break;
                case "%":
//                    num /= 100;
                    num = num.divide(new BigDecimal(100));
                    break;
                default:
                    break;
            }
            result.add(num);
        }
        return result;
    }


    /**
     * 从字符串中抽取出不带掩码的身份证号码,如果没抽到，返回null。
     * 仅抽取18位/15位国内身份证
     *
     * @param str
     * @return
     */
    public static String extractCNIDNotMask(String str) {
        Matcher matcher = PATTERN_MATCH_CNDI.matcher(str);
        while (matcher.find()) {
            String cnid = matcher.group("cnid");
            if (StringUtil.isNotEmpty(cnid)) {
                return cnid;
            }
        }
        return null;
    }

    /**
     * 从字符串中抽取出带掩码的身份证号码,如果没抽到，返回null。
     * 仅抽取18位/15位国内身份证，且月日4位掩码或末尾4位掩码
     *
     * @param str
     * @return
     */
    public static String extractCNIDWithMask(String str) {
        Matcher matcher = PATTERN_MATCH_CNDI.matcher(str);
        while (matcher.find()) {
            String cnidWithMask = matcher.group("cnidWithMask");
            if (StringUtil.isNotEmpty(cnidWithMask)) {
                return cnidWithMask;
            }
        }
        return null;
    }

    /**
     * 从字符串中抽取出带掩码的身份证号码,如果没抽到，返回null。
     * 支持有掩码或无掩码
     *
     * @param str
     * @return
     */
    public static String extractCNID(String str) {
        Matcher matcher = PATTERN_MATCH_CNDI.matcher(str);
        while (matcher.find()) {
            String cnid = matcher.group("cnid");
            if (StringUtil.isNotEmpty(cnid)) {
                return cnid;
            }
            String cnidWithMask = matcher.group("cnidWithMask");
            if (StringUtil.isNotEmpty(cnidWithMask)) {
                return cnidWithMask;
            }
        }
        return null;
    }

    /**
     * 匹配里面所有数字
     *
     * @param str
     * @return
     */
    public static String extractAllNumber(String str) {
        String result = "";
        Matcher matcher = PATTERN_MATCH_ALL_NUMBER.matcher(str);
        while (matcher.find()) {
            result += matcher.group();
        }
        return result;
    }

    public static List<String> extractSqlList(String text){
        return new SqlExtractor().extract(text);
    }

    private static class SqlExtractor {
        private List<String> result = new ArrayList<>();

        /**
         * 正在拼接的 sql
         */
        private StringBuilder sqlBuilder = new StringBuilder(128);

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
}
