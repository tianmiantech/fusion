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

package com.welab.wefe.common.util;

import cn.hutool.core.io.IoUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zane.luo
 */
public class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static boolean isImage(File file) {
        if (file.isDirectory()) {
            return false;
        }
        return isImage(file.getName());
    }

    public static boolean isExcel(File file) {
        switch (getFileSuffix(file.getName()).toLowerCase()) {
            case "xlsx":
            case "xls":
                return true;
            default:
                return false;
        }
    }

    public static boolean isImage(String filename) {
        switch (getFileSuffix(filename).toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "webp":
            case "bmp":
            case "tif":
            case "gif":
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否是压缩包文件
     */
    public static boolean isArchive(File file) {
        switch (getFileSuffix(file).toLowerCase()) {
            case "zip":
            case "tar":
            case "gz":
            case "tgz":
            case "7z":
            case "rar":
                return true;
            default:
                return false;
        }
    }

    public static String getFileSuffix(File file) {
        if (file.isDirectory()) {
            return null;
        }
        return getFileSuffix(file.getName());
    }

    /**
     * get file suffix
     */
    public static String getFileSuffix(String filename) {
        return StringUtil.substringAfterLast(filename, ".");
    }

    /**
     * get file name without suffix
     */
    public static String getFileNameWithoutSuffix(File file) {
        if (file.isDirectory()) {
            return "";
        }
        return getFileNameWithoutSuffix(file.getName());
    }

    public static String getFileNameWithoutSuffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        return StringUtil.substringBeforeLast(fileName, ".");
    }

    /**
     * Create a directory
     *
     * @param dirPath Directory path
     */
    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    public static String readAllText(String path) throws IOException {
        return readAllText(new File(path), StandardCharsets.UTF_8);
    }

    public static String readAllText(File file) throws IOException {
        return readAllText(file, StandardCharsets.UTF_8);
    }


    public static String readAllText(File file, Charset charset) throws IOException {
        StringBuilder content = new StringBuilder(512);
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, charset);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                content
                        .append(line)
                        .append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw e;
        } finally {
            in.close();
        }
        return content.toString();
    }

    /**
     * Delete files or folders
     *
     * @param file File or folder
     */
    public static void deleteFileOrDir(File file) {
        if (null == file) {
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        File[] subFile = file.listFiles();
        for (File f : subFile) {
            deleteFileOrDir(f);
        }
        file.delete();
    }

    public static void deleteFileOrDir(String filePath) {
        deleteFileOrDir(new File(filePath));
    }

    /**
     * 将文本写入到文件（utf-8编码）
     *
     * @param text   要写入的文本内容
     * @param path   文件路径
     * @param append 是否追加，如果不追加，会覆盖已有文件。
     */
    public static void writeTextToFile(String text, Path path, boolean append) throws IOException {
        createDir(path.getParent().toString());
        if (!append) {
            File file = path.toFile();
            if (file.exists()) {
                file.delete();
            }
        }
        Files.write(
                path,
                text.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE
        );
    }

    public static void copy(Path source, Path target, CopyOption... options) throws IOException {
        createDir(target.getParent().toString());
        Files.copy(source, target, options);
    }


    public static void moveFile(File file, String distDir) {
        moveFile(file, Paths.get(distDir));
    }

    public static void moveFile(File file, Path distDir) {
        // 文件已经在目标目录，不用移动。
        if (file.getParentFile().toPath().equals(distDir)) {
            return;
        }

        String fileName = file.getName();

        distDir.toFile().mkdirs();

        File distFile = distDir.resolve(fileName).toFile();
        if (distFile.exists()) {
            distFile.delete();
        }
        file.renameTo(distFile);
    }

    /**
     * Reading file contents
     *
     * @return String
     */
    public static List<String> readAllForLine(String path, String encoding) throws IOException {
        List<String> lineList = new ArrayList<>();
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, encoding);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (null != in) {
                in.close();
            }

        }
        return lineList;
    }

    public static void saveJarResource2File(String resourcePath, File distFile) throws IOException {
        if (distFile.exists()) {
            distFile.delete();
        }
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        FileOutputStream outputStream = new FileOutputStream(distFile);
        byte[] bytes = new byte[1024];
        while ((inputStream.read(bytes)) != -1) {
            // 写入数据
            outputStream.write(bytes);
            outputStream.flush();
        }
        inputStream.close();
        // 保存数据
        outputStream.flush();
        outputStream.close();

    }

    /**
     * 获取目录下的文件名或子目录名称列表
     */
    public static List<String> getSubFileNames(String dir, boolean includeSubDir) {
        List<String> subFileNames = new ArrayList<>();
        if (StringUtil.isEmpty(dir)) {
            return subFileNames;
        }
        File[] subFiles = new File(dir).listFiles();
        if (null != subFiles) {
            for (int i = 0; i < subFiles.length; i++) {
                File subFile = subFiles[i];
                if (!subFile.isFile()) {
                    if (includeSubDir) {
                        subFileNames.add(subFile.getName());
                    }
                    continue;
                }
                subFileNames.add(subFile.getName());
            }
        }
        return subFileNames;
    }


    public static void mergeFiles(List<File> srcFileList, File dstFile) throws Exception {
        if (CollectionUtils.isEmpty(srcFileList) || null == dstFile) {
            return;
        }

        if (!dstFile.exists()) {
            dstFile.createNewFile();
        }
        for (File srcFile : srcFileList) {
            IoUtil.copy(new FileInputStream(srcFile), new FileOutputStream(dstFile, true));
        }
    }

    public static void deleteFileOrDir(List<File> fileList) {
        if (CollectionUtils.isEmpty(fileList)) {
            return;
        }
        for (File file : fileList) {
            deleteFileOrDir(file);
        }
    }

    /**
     * 递归获取指定文件夹下的所有文件
     *
     * @param path         路径
     * @param withChildren 是否包含子文件夹
     */
    public static List<File> listFiles(Path path, boolean withChildren) {
        List<File> result = new ArrayList<>();

        // 目录不存在
        if (path == null || !path.toFile().exists()) {
            return result;
        }

        File[] files = path.toFile().listFiles();
        // 这里可能会为 null，要做空指针检查。
        if (files == null) {
            return result;
        }

        for (File file : files) {
            listFiles(file, withChildren, result);
        }

        return result;
    }

    /**
     * 目录的文件列表(不包含子目录)
     */
    public static List<File> subFiles(File dirFile, String subFileSuffix) {
        List<File> result = new ArrayList<>();
        if (null == dirFile || !dirFile.exists() || !dirFile.isDirectory()) {
            return result;
        }
        File[] subFileList = dirFile.listFiles();
        if (null == subFileList) {
            return result;
        }
        for (int i = 0; i < subFileList.length; i++) {
            File file = subFileList[i];
            if (file.isDirectory()) {
                continue;
            }
            if (StringUtil.isEmpty(subFileSuffix) || file.getName().endsWith(subFileSuffix)) {
                result.add(file);
            }
        }

        return result;
    }


    /**
     * 递归获取指定文件夹下的所有文件
     *
     * @param file         文件或文件夹
     * @param withChildren 是否包含子文件夹
     * @param result       汇总的文件列表
     */
    private static void listFiles(File file, boolean withChildren, List<File> result) {

        if (file.isDirectory() && withChildren) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    listFiles(f, withChildren, result);
                }
            }

        }

        if (file.isFile()) {
            result.add(file);
        }
    }

    /**
     * 读取 resource/ 文件夹中的文件
     * <p>
     * e.g: project_flow_template/manifest.json
     */
    public static String readFileFromResource(String fileName) {
        String content;
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            content = IOUtils.toString(
                    inputStream,
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content;
    }

    /**
     * 创建文件
     */
    public static void createFile(File file) throws IOException {
        if (null == file || file.exists()) {
            return;
        }
        File parentFile = new File(file.getParent());
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
    }

    /**
     * 获取文件创建时间
     */
    public static Date getCreateTime(File file) throws IOException {
        // 根据path获取文件的基本属性类
        BasicFileAttributes attrs = Files.readAttributes(
                file.toPath(),
                BasicFileAttributes.class
        );
        return new Date(attrs.creationTime().toMillis());
    }

    /**
     * 获取文件行数
     *
     * 最终行数会剔除掉文件尾部的空行行数
     */
    public static long getFileLineCount(File file) {
        long totalRowCount = 0;
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file))) {
            lineNumberReader.skip(Long.MAX_VALUE);
            // 计算行数时，不包含列头。
            totalRowCount = lineNumberReader.getLineNumber() - 1L;
        } catch (IOException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return 0;
        }

        // 如果最后一行是空行，行数减一。
        if (totalRowCount > 0) {
            try (ReversedLinesFileReader reversedLinesReader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
                String lastLine;

                // 去除空行
                while (true) {
                    lastLine = reversedLinesReader.readLine();

                    if (StringUtil.isBlank(lastLine)) {
                        totalRowCount--;
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
        }

        return totalRowCount;
    }

    public static BufferedReader buildBufferedReader(File file) throws FileNotFoundException {
        return new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file),
                        StandardCharsets.UTF_8
                )
        );
    }

    public static BufferedWriter buildBufferedWriter(File file) throws FileNotFoundException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file, false),
                        StandardCharsets.UTF_8
                )
        );
    }

    public static void main(String[] args) throws IOException {
        // 文件储存路径
       /* File file = new File("D://test.txt");
        System.out.println(file.getName().substring(0, ".txt".length()));
        // 将 jar 包内的资源保存为文件
        saveJarResource2File("test/pom.xml", file);

        System.out.println(readAllText(file));*/

        createFile(new File("D:\\aaa\\bbb\\c.txt"));
    }
}
