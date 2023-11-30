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
package com.welab.fusion.service.job_function;

import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.DownloadChunkApi;
import com.welab.fusion.service.api.download.GetDownloadFileInfoApi;
import com.welab.fusion.service.api.download.base.FileType;
import com.welab.fusion.service.service.GatewayService;
import com.welab.fusion.service.service.MemberService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/11/29
 */
public class DownloadPartnerPsiBloomFilterFunction implements com.welab.fusion.core.function.DownloadPartnerPsiBloomFilterFunction {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final MemberService memberService = Launcher.getBean(MemberService.class);
    private static final GatewayService gatewayService = Launcher.getBean(GatewayService.class);

    @Override
    public File download(String jobId, String partnerId, Consumer<Long> totalSizeConsumer, Consumer<Long> downloadSizeConsumer) throws Exception {
        LOG.info("开始下载过滤器文件，memberId：{}，jobId：{}", partnerId, jobId);

        long start = System.currentTimeMillis();
        FusionNodeInfo partner = memberService.findById(partnerId).toFusionNodeInfo();

        GetDownloadFileInfoApi.Input input = GetDownloadFileInfoApi.Input.of(
                FileType.BloomFilter,
                JObject.create("jobId", jobId)
        );

        GetDownloadFileInfoApi.Output fileInfo = gatewayService.callOtherFusionNode(
                partner,
                GetDownloadFileInfoApi.class,
                input,
                GetDownloadFileInfoApi.Output.class
        );

        // 为避免文件名相同造成覆盖，重新设计落地时的文件名。
        String distFilename = "member_" + partnerId + "-bf_" + fileInfo.filename;
        Path distDir = FileSystem.getTempDir()
                .resolve(
                        distFilename.replace(".", "_")
                );
        distDir.toFile().mkdirs();

        // 分片下载
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
        }

        LOG.info("过滤器文件下载完成，memberId：{}，jobId：{}，耗时：{}ms", partnerId, jobId, System.currentTimeMillis() - start);

        // 合并分片
        File[] parts = distDir.toFile().listFiles();
        File mergedFile = FileSystem.getTempDir()
                .resolve(distFilename)
                .toFile();

        if (mergedFile.exists()) {
            mergedFile.delete();
        }

        for (int i = 1; i <= parts.length; i++) {
            File part = distDir.resolve(i + ".part").toFile();

            FileOutputStream stream = new FileOutputStream(mergedFile, true);
            FileUtils.copyFile(part, stream);
            stream.close();
        }

        // 合并完毕删除分片
        FileUtils.deleteDirectory(distDir.toFile());

        // 更新最后修改时间，避免被垃圾文件清理模块删除。
        mergedFile.setLastModified(System.currentTimeMillis());

        if (mergedFile.length() != fileInfo.fileLength) {
            StatusCode.FILE_IO_WRITE_ERROR
                    .throwException("过滤器文件下载失败，期望大小：" + fileInfo.fileLength + "，实际大小：" + mergedFile.length() + "。");
        }

        return mergedFile;
    }
}
