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

import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.OS;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class FileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystem.class);
    private static Path ROOT_DIR;

    public static void init(String fileSystemBaseDir) throws IOException {
        init(fileSystemBaseDir, null);
    }

    /**
     * 初始化，指定根目录。
     *
     * @param fileSystemBaseDir 根目录
     * @param serverPort        服务端口，用于区分不同的服务，避免在单机上部署多个服务时，文件目录冲突。
     */
    public static void init(String fileSystemBaseDir, String serverPort) throws IOException {
        // 当未指定时，设置默认值。
        if (StringUtil.isEmpty(fileSystemBaseDir)) {
            fileSystemBaseDir = OS.get() == OS.windows
                    ? "D:\\data\\fusion"
                    : "/data/fusion";
        }

        ROOT_DIR = Paths.get(fileSystemBaseDir);

        if (StringUtil.isNotBlank(serverPort)) {
            ROOT_DIR = ROOT_DIR.resolve(serverPort);
        }

        // 创建目录
        File dir = ROOT_DIR.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 测试权限
        Path testFilePath = ROOT_DIR.resolve("test.txt");
        FileUtil.writeTextToFile("write is ok!", testFilePath, false);
        testFilePath.toFile().delete();
    }


    public static Path getRootDir() {
        return ROOT_DIR;
    }

    /**
     * 处于安全考虑，不对外输出绝对路径，而是输出相对路径。
     */
    public static String getRelativePath(File file) {
        return ROOT_DIR.relativize(file.toPath()).toString();
    }

    /**
     * 文件用途
     */
    public enum UseType {
        /**
         * 临时目录，文件不会长时间保存。
         */
        Temp,
        JobTemp,
        PsiBloomFilter,
        PsiECEncryptedData,
        PsiSecondaryECEncryptedData,
        /**
         * 求交结果
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
        Path path = getRootDir().resolve(childDir);
        // 自动创建目录
        path.toFile().mkdirs();
        return path;
    }

    public static class JobTemp {
        public static Path getDir(String jobId) {
            return getBaseDir(UseType.JobTemp).resolve(jobId);
        }

        public static File getFile(String jobId, JobPhase jobPhase, String memberId, String filename) {
            return getDir(jobId)
                    .resolve(
                            "member_" + memberId.replace(":", "_")
                                    + "-" + jobPhase
                                    + "_" + new File(filename).getName()
                    )
                    .toFile();
        }

        /**
         * 全量的原始数据
         */
        public static File getAllOriginalData(String jobId) {
            return getDir(jobId)
                    .resolve("AllOriginalData.csv")
                    .toFile();
        }

        /**
         * 交集部分的原始数据
         */
        public static File getIntersectionOriginalData(String jobId) {
            return getDir(jobId)
                    .resolve("IntersectionOriginalData.csv")
                    .toFile();
        }


        public static File getFileOnlyKey(String jobId) {
            return getDir(jobId).resolve(jobId + "-only_key.csv").toFile();
        }

        public static File getFileOnlyKeyColumns(String jobId) {
            return getDir(jobId).resolve(jobId + "-only_key_columns.csv").toFile();
        }

        public static File getFileWithMyselfAdditionalColumns(String jobId) {
            return getDir(jobId).resolve(jobId + "-with_myself_additional_columns.csv").toFile();
        }

        /**
         * 删除目录
         */
        public static void clean(String jobId) {
            File dir = getDir(jobId).toFile();
            try {
                cn.hutool.core.io.FileUtil.clean(dir);
                dir.delete();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
        }
    }

    public static class PsiBloomFilter {
        /**
         * 获取指定布隆过滤器所在目录
         */
        public static Path getDir(String id) {
            return getBaseDir(UseType.PsiBloomFilter).resolve(id);
        }

    }

    public static class FusionResult {
        public static File getResultFile(String jobId) {
            return getBaseDir(UseType.FusionResult).resolve(jobId + "-result.csv").toFile();
        }
    }

    public static class PsiECEncryptedData {
        public static Path getDir(String id) {
            return getBaseDir(UseType.PsiECEncryptedData).resolve(id);
        }
    }

    public static class PsiSecondaryECEncryptedData {
        public static File getDataFile(String jobId) {
            return getBaseDir(UseType.PsiSecondaryECEncryptedData)
                    .resolve(jobId + ".data")
                    .toFile();
        }
    }
}
