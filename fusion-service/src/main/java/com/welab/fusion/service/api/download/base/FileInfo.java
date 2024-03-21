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
package com.welab.fusion.service.api.download.base;

import com.alibaba.fastjson.JSON;
import com.welab.fusion.core.io.FileSystem;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.io.File;
import java.io.IOException;

/**
 * @author zane.luo
 * @date 2023/12/4
 */
public class FileInfo {
    @Check(name = "文件名", require = true)
    public String filename;
    @Check(name = "文件路径", require = true)
    public String filePath;
    @Check(name = "文件大小（byte）", require = true)
    public long fileLength;
    @Check(name = "分片大小", require = true)
    public long chunkSizeInMb = DownloadConfig.CHUNK_SIZE_IN_MB;
    @Check(name = "分片数量", require = true)
    public int chunkCount;

    public static FileInfo of(File file) {
        FileInfo output = new FileInfo();
        output.filename = file.getName();
        output.filePath = FileSystem.getRelativePath(file);
        output.fileLength = file.length();
        output.chunkCount = (int) Math.ceil(output.fileLength / (double) (output.chunkSizeInMb * 1024 * 1024));
        return output;
    }

    public static void main(String[] args) throws IOException {
        FileSystem.init("D:\\data\\wefe");
        File file = new File("D:\\data\\wefe\\ivenn_10w_20210319_vert_promoter.csv");
        System.out.println(JSON.toJSONString(of(file), true));
    }
}
