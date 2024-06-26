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
package com.welab.fusion.service.api.download;

import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.base.FileInfo;
import com.welab.fusion.service.service.GatewayService;
import com.welab.fusion.service.service.MemberService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zane.luo
 * @date 2023/12/4
 */
public class Downloader {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final MemberService memberService = Launcher.getBean(MemberService.class);
    private static final GatewayService gatewayService = Launcher.getBean(GatewayService.class);
    private final JobPhase jobPhase;
    /**
     * 下载后的文件输出路径
     */
    Function<FileInfo, File> distFileFileGetter;
    private String partnerId;
    private String jobId;
    private Consumer<Long> totalSizeConsumer;
    private Consumer<Long> completedSizeConsumer;

    public Downloader(JobPhase jobPhase, String jobId, String partnerId, Function<FileInfo, File> distFileFileGetter) {
        this.jobPhase = jobPhase;
        this.partnerId = partnerId;
        this.jobId = jobId;
        this.distFileFileGetter = distFileFileGetter;
    }

    public File download() throws IOException, StatusCodeWithException {
        LOG.info("开始从合作方下载文件({})，memberId：{}，jobId：{}", jobPhase, partnerId, jobId);

        long start = System.currentTimeMillis();
        FusionNodeInfo partner = memberService.findById(partnerId).toFusionNodeInfo();

        FileInfo fileInfo = getFileInfo(partner);

        if (totalSizeConsumer != null) {
            totalSizeConsumer.accept(fileInfo.fileLength);
        }

        // 为避免文件名相同造成覆盖，重新设计落地时的文件名。
        File distFile = distFileFileGetter.apply(fileInfo);
        // 先将分片下载到临时目录，合并分片后再移动到目标目录。
        Path tempDir = FileSystem.getTempDir()
                .resolve(
                        distFile.getName().replace(".", "_")
                );
        tempDir.toFile().delete();
        tempDir.toFile().mkdirs();

        if (fileInfo.fileLength > 0) {
            // 分片下载
            downloadChunks(fileInfo, partner, tempDir);

            // 合并分片
            mergeChunks(tempDir, distFile, fileInfo);
        }
        // 文件大小为0时，直接创建空文件。
        else {
            distFile.getParentFile().mkdirs();
            distFile.createNewFile();
        }

        LOG.info("从合作方下载文件完成({})，memberId：{}，jobId：{}，耗时：{}ms", jobPhase, partnerId, jobId, System.currentTimeMillis() - start);

        return distFile;
    }

    /**
     * 合并分片
     *
     * @param dir      分片所在目录
     * @param distFile 合并后的文件路径
     * @param fileInfo 文件信息
     */
    private File mergeChunks(Path dir, File distFile, FileInfo fileInfo) throws IOException, StatusCodeWithException {
        File[] parts = dir.toFile().listFiles();

        distFile.getParentFile().mkdirs();
        if (distFile.exists()) {
            distFile.delete();
        }

        for (int i = 0; i < parts.length; i++) {
            File part = dir.resolve(i + ".part").toFile();

            FileOutputStream stream = new FileOutputStream(distFile, true);
            FileUtils.copyFile(part, stream);
            stream.close();
        }

        // 合并完毕删除分片
        FileUtils.deleteDirectory(dir.toFile());

        // 更新最后修改时间，避免被垃圾文件清理模块删除。
        distFile.setLastModified(System.currentTimeMillis());

        if (distFile.length() != fileInfo.fileLength) {
            StatusCode.FILE_IO_WRITE_ERROR
                    .throwException(
                            "从合作方下载文件失败(" + jobPhase
                                    + ")，期望大小：" + fileInfo.fileLength +
                                    "，实际大小：" + distFile.length() + "。"
                    );
        }
        return distFile;
    }

    /**
     * 分片下载
     */
    private void downloadChunks(FileInfo fileInfo, FusionNodeInfo partner, Path distDir) throws StatusCodeWithException, IOException {
        long completedSize = 0;
        for (int i = 0; i < fileInfo.chunkCount; i++) {

            HttpResponse httpResponse = gatewayService.requestOtherFusionNode(
                    partner,
                    DownloadChunkApi.class,
                    DownloadChunkApi.Input.of(fileInfo.filePath, i).toJson()
            );

            Files.write(
                    distDir.resolve(i + ".part"),
                    httpResponse.getBodyBytes()
            );

            // 更新进度
            if (completedSizeConsumer != null) {
                completedSize += httpResponse.getBodyBytes().length;
                completedSizeConsumer.accept(completedSize);
            }
        }
    }

    /**
     * 获取文件信息
     */
    private FileInfo getFileInfo(FusionNodeInfo partner) throws StatusCodeWithException {
        FileInfo fileInfo = gatewayService.callOtherFusionNode(
                partner,
                GetDownloadFileInfoApi.class,
                GetDownloadFileInfoApi.Input.of(jobPhase, jobId)
        );
        return fileInfo;
    }

    public void setTotalSizeConsumer(Consumer<Long> totalSizeConsumer) {
        this.totalSizeConsumer = totalSizeConsumer;
    }

    public void setCompletedSizeConsumer(Consumer<Long> completedSizeConsumer) {
        this.completedSizeConsumer = completedSizeConsumer;
    }
}
