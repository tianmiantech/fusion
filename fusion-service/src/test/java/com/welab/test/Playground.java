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
package com.welab.test;

import cn.hutool.core.util.StrUtil;

/**
 * @author zane.luo
 * @date 2023/11/8
 */
public class Playground {
    public static void main(String[] args) {
        String[] urls = {
                "http://localhost:8080/fusion/website",
                "http://localhost:8080/fusion/website/",
                "http://localhost:8080/fusion/website/index.html",
                "http://localhost:8080/fusion/website/hello/world/index.html?a=b&b=c"
        };

        for (String url : urls) {
            String path = StrUtil.subAfter( url,"/website",false);
            path = path.replace("//","/");
            String fileName = StrUtil.subBefore(path,"?",false);
            if (fileName.startsWith("/")){
                fileName = fileName.substring(1);
            }
            if (StrUtil.isEmpty(fileName)){
                fileName = "index.html";
            }
            System.out.println(fileName);
        }

    }
}
