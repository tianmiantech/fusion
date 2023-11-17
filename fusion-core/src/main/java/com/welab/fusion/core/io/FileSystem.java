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
package com.welab.fusion.core.io;

import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class FileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystem.class);
    private static Path ROOT_DIR;

    /**
     * 初始化，指定根目录。
     */
    public static void init(Path rootDir) {
        ROOT_DIR = rootDir;
        // 创建目录
        File dir = ROOT_DIR.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    public static Path getRootDir() {
        return ROOT_DIR;
    }

    /**
     * 文件用途
     */
    public enum UseType {
        /**
         * 临时目录，文件不会长时间保存。
         */
        Temp,
        PsiBloomFilter,
        /**
         * 融合结果
         */
        FusionResult
    }

    /**
     * 获取临时目录
     */
    public static Path getTempDir() {
        return getBaseDir(UseType.Temp);
    }

    /**
     * 获取各用途文件的基础路径
     */
    public static Path getBaseDir(UseType type) {
        String childDir = StringUtil.stringToUnderLineLowerCase(type.name());
        return getRootDir().resolve(childDir);
    }

    public static class PsiBloomFilter {
        /**
         * 获取指定布隆过滤器所在目录
         */
        public static Path getPath(String id) {
            return getBaseDir(UseType.PsiBloomFilter).resolve(id);
        }

    }
}
