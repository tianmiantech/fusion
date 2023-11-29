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
package com.welab.fusion.service.api.file.security;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zane
 * @date 2021/12/31
 */
public abstract class FileSecurityScanner {
    protected final static Logger LOG = LoggerFactory.getLogger(FileSecurityScanner.class);
    protected static final String[] keywords = {"<", ">", "\\"};
    /**
     * 允许的文件类型
     */
    private static final List<String> ALLOW_FILE_TYPES = Arrays.asList(
            "xls", "xlsx", "csv",
            "zip", "gz", "tgz", "7z",
            "jpg", "jpeg", "png"
    );

    public static final ExpiringMap<String, ScanResult> SCAN_RESULT_MAP = ExpiringMap
            .builder()
            .expiration(60, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .maxSize(50)
            .build();

    protected abstract void doScan(File file) throws Exception;

    public static String createScanSession() {
        String scanSessionId = UUID.randomUUID().toString();
        SCAN_RESULT_MAP.put(scanSessionId, ScanResult.of());
        return scanSessionId;
    }

    public static void scan(String scanSessionId, File file) {

        ScanResult scanResult = SCAN_RESULT_MAP.get(scanSessionId);

        if (scanResult == null) {
            throw new RuntimeException("在执行扫描前，请先调用 createScanSession() 方法创建扫描会话。");
        }

        // 为检查上传的文件是否安全
        String suffix = StringUtil.substringAfterLast(file.getName(), ".");
        try {
            checkIsAllowFileType(file.getName());

            switch (suffix) {
                case "xls":
                case "xlsx":
                    new ExcelSecurityScanner().doScan(file);
                    break;
                case "csv":
                    new CsvSecurityScanner().doScan(file);
                    break;
                case "zip":
                case "gz":
                case "tgz":
                case "7z":
                    break;
                case "jpg":
                case "jpeg":
                case "png":
                    new ImageSecurityScanner().doScan(file);
                    break;
                default:
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
            }

            scanResult.success();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            FileUtil.deleteFileOrDir(file);
            scanResult.fail(e.getMessage());
        }
    }

    public static void checkIsAllowFileType(String filename) throws StatusCodeWithException {
        if (StringUtil.isEmpty(filename)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("文件名不允许为空");
        }

        String suffix = StringUtil.substringAfterLast(filename, ".");
        if (StringUtil.isEmpty(suffix)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("不支上传无文件后缀的文件");
        }

        if (!ALLOW_FILE_TYPES.contains(suffix)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
        }
    }
}
