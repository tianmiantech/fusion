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
package com.welab.wefe.common.web.api_document;

import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api_document.model.ApiItem;
import com.welab.wefe.common.web.api_document.model.ApiParam;
import com.welab.wefe.common.web.api_document.model.ApiParamField;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zane
 * @date 2021/12/3
 */
public class HtmlFormatter extends AbstractApiDocumentFormatter {
    StringBuilder str = new StringBuilder(2048);
    private Map<Class<?>, String> enumTypeDicMap = new HashMap<>();
    private Map<Class<?>, String> classTypeDicMap = new HashMap<>();

    public HtmlFormatter() {
        setToc();
    }

    private void setToc() {
        str.append("<ol>");
        for (ApiItem api : API_LIST) {
            str.append("<li>");
            str.append(
                    "<a href='#" + api.id + "'>" +
                            api.path +
                            (StringUtil.isEmpty(api.name) ? "" : "(" + api.name + ")") +
                            "</a>"
            );
            str.append("</li>");
        }
        str.append("</ol>");
    }

    @Override
    public String contentType() {
        return "text/html";
    }

    @Override
    protected void formatApiItem(ApiItem api) {
        str.append("<div class=\"api-item\" value='" + api.group() + "'>\n" +
                "<h3 id='" + api.id + "' class=\"api-name\">" +
                api.path +
                (StringUtil.isEmpty(api.name) ? "" : "(" + api.name + ")") +
                "</h3>\n");

        if (StringUtil.isNotEmpty(api.desc)) {
            str
                    .append("<p class=\"api-desc\">")
                    .append(api.desc)
                    .append("</p>\n");
        }

        str
                .append("<div class='api-params'>")
                .append(getParams("入参", api.input))
                .append(getParams("响应", api.output))
                .append("</div>")
                .append("</div>");
    }

    @Override
    protected void formatGroupItem(String name) {
        str.append("<h2 class=\"group-title\"  value='" + name + "'>").append(name).append("</h2>");
    }

    @Override
    protected String getOutput() {
        setTypeDic();

        String html = str.toString();
        return FileUtil
                .readFileFromResource("api_doc/index.html")
                .replace("$content$", html);

    }

    private void setTypeDic() {
        str
                .append("<div id='type-dic-map'>")
                .append("<h2>数据类型字典</h2>");

        for (Map.Entry<Class<?>, String> entry : enumTypeDicMap.entrySet()) {
            str.append(entry.getValue());
        }
        str.append("<div class='clear-both'></div>");

        str.append("<div class='class-type-dic-list'>");
        for (Map.Entry<Class<?>, String> entry : classTypeDicMap.entrySet()) {
            str.append(entry.getValue());
        }
        str.append("</div>");
        str.append("<div class='clear-both'></div>");

        str.append("</div>");
    }

    private String getParams(String title, ApiParam params) {
        if (params == null) {
            return "";
        }
        String output = "</br>" +
                "<table class=\"api-param-table\">\n" +
                "<caption>" +
                title +
                "</caption>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th style=\"width:200px\">name</th>\n" +
                "<th style=\"width:200px\">type</th>\n" +
                "<th style=\"width:50px\">require</th>\n" +
                "<th style=\"width:20%\">comment</th>\n" +
                "<th style=\"\">desc</th>\n" +
                "<th style=\"width:200px\">regex</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n";
        for (ApiParamField item : params.fields) {
            output += "<tr>\n" +
                    "<td>" + item.name + "</td>\n" +
                    "<td>" + renderTypeName(item) + "</td>\n" +
                    "<td style=\"text-align: center;\">" + item.require + "</td>\n" +
                    "<td>" + item.comment + "</td>\n" +
                    "<td>" + item.desc + "</td>\n" +
                    "<td>" + item.regex + "</td>\n" +
                    "</tr>\n";
        }
        output += "</tbody>\n" +
                "</table>\n";
        return output;
    }


    private String renderTypeName(ApiParamField item) {
        if (isSimpleType(item.typeClass)) {
            return item.typeName;
        }

        String id = classToId(item.typeClass);
        if (enumTypeDicMap.containsKey(item.typeClass)) {
            return "<a href='#" + id + "'>" + item.typeName + "</a>";
        }

        if (item.typeClass.isEnum()) {
            enumTypeDicMap.put(item.typeClass, renderEnumTypeDic(item.typeClass));
        } else {
            classTypeDicMap.put(item.typeClass, renderClassTypeDic(item.typeClass));
        }

        return "<a href='#" + id + "'>" + item.typeName + "</a>";
    }

    private String renderClassTypeDic(Class<?> type) {
        String id = classToId(type);
        StringBuilder html = new StringBuilder(256);
        html
                .append("<div id='" + id + "' class='class-dic-panel'>")
                .append(getParams(type.getSimpleName(), new ApiParam(type)))
                .append("</div>");

        return html.toString();
    }

    private String renderEnumTypeDic(Class<?> type) {
        String id = classToId(type);
        StringBuilder html = new StringBuilder(128);
        html
                .append("<div id='" + id + "' class='enum-dic-panel'>")
                .append("<p class='enum-title'>" + type.getSimpleName() + "</p>")
                .append("<ul>");

        for (Object constant : type.getEnumConstants()) {
            html
                    .append("<li>")
                    .append(constant.toString())
                    .append("</li>");
        }

        html
                .append("</ul>")
                .append("</div>");

        return html.toString();
    }

    private boolean isSimpleType(Class<?> type) {
        if (type.isEnum()) {
            return false;
        }

        if (type.getName().contains("welab")) {
            return false;
        }

        return true;
    }

    private String classToId(Class<?> clazz) {
        return clazz.getName().replace("#", ".");
    }


}
