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
package com.welab.fusion.core.algorithm.base.phase_action;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.base.JobRole;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.algorithm.base.PsiAlgorithm;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.util.Constant;
import com.welab.wefe.common.util.CloseableUtils;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/13
 */
public class P9SaveResultAction<T extends AbstractPsiJob> extends AbstractJobPhaseAction<T> {
    private static final int batchSize = 100_000;
    private BufferedWriter writer;
    private LinkedHashSet<String> myselfHeader;
    private LinkedHashSet<String> partnerHeader;
    private LongAdder progress = new LongAdder();

    public P9SaveResultAction(T job) {
        super(job);
    }

    @Override
    protected void doAction() throws Exception {
        phaseProgress.setMessageAndLog("正在生成最终求交结果文件...");

        // 过滤器方由于缺少原始明文数据，所以无法根据 key 还原为原始字段，只能输出 key。
        if (job.getAlgorithm() == PsiAlgorithm.rsa_psi
                && job.getMyJobRole() == JobRole.follower
                && job.getMyself().dataResourceInfo.dataResourceType == DataResourceType.PsiBloomFilter) {

            File source = job.getJobTempData().resultFileWithPartnerAdditionalColumns == null
                    ? job.getJobTempData().resultFileOnlyKey
                    : job.getJobTempData().resultFileWithPartnerAdditionalColumns;

            File target = FileSystem.FusionResult.getResultFile(job.getJobId());
            FileUtil.copy(
                    source.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            );

        } else {
            createResultFile();
        }


        // 储存结果
        job.getJobFunctions().saveFusionResultFunction.save(
                job.getJobId(),
                job.getMyJobRole(),
                job.getJobResult(),
                totalSize -> phaseProgress.updateTotalWorkload(totalSize),
                downloadSize -> phaseProgress.updateCompletedWorkload(downloadSize)
        );

        phaseProgress.setMessageAndLog("结果已保存");
    }

    /**
     * 生成最终的结果文件
     *
     * 最终文件包含的列：
     * 1. 我方生成 Key 相关的字段
     * 2. 对方的附加结果字段
     */
    private void createResultFile() throws Exception {
        this.writer = initFile();

        int partitionIndex = 0;
        while (true) {
            List<LinkedHashMap<String, Object>> partition = FileUtil.readPartitionRows(
                    job.getJobTempData().intersectionOriginalData,
                    partitionIndex,
                    batchSize
            );
            partitionIndex++;

            if (partition.isEmpty()) {
                break;
            }

            writeOnePartition(partition);

            if (partition.size() < batchSize) {
                break;
            }
        }
    }

    private void writeOnePartition(List<LinkedHashMap<String, Object>> originalDataPartition) throws IOException {
        for (LinkedHashMap<String, Object> originalData : originalDataPartition) {
            // 拼接我方 Key 相关字段
            String line = super.rowToCsvLine(getMyselfHeader(), originalData, false);

            // 拼接协作方附加字段
            if (!getPartnerHeader().isEmpty()) {
                String key = originalData.get(Constant.KEY_COLUMN_NAME).toString();
                LinkedHashMap<String, Object> partnerRow = findPartnerRow(key);
                if (partnerRow != null) {
                    line += "," + super.rowToCsvLine(getPartnerHeader(), partnerRow, false);
                }
            }

            line += System.lineSeparator();
            writer.write(line);

            progress.increment();
            phaseProgress.updateCompletedWorkload(progress.longValue());
        }

    }

    private LinkedHashMap<String, Object> findPartnerRow(String key) throws IOException {
        File file = job.getJobTempData().resultFileWithPartnerAdditionalColumns;
        CsvReader reader = new CsvReader();
        reader.setContainsHeader(false);
        reader.setSkipEmptyRows(true);
        CsvParser parser = reader.parse(file, StandardCharsets.UTF_8);
        CsvRow headerRow = parser.nextRow();
        if (headerRow == null) {
            return null;
        }
        List<String> header = headerRow.getFields();

        while (true) {
            CsvRow row = parser.nextRow();


            if (row == null) {
                return null;
            }

            if (key.equals(row.getField(0))) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < header.size(); i++) {
                    String value = "";
                    if (row.getFieldCount() > i) {
                        value = row.getField(i);
                    }
                    map.put(header.get(i), value);
                }
                return map;
            }
        }
    }


    /**
     * 初始化结果文件
     */
    private BufferedWriter initFile() throws Exception {
        if (job.getJobResult().resultFile != null) {
            throw new RuntimeException("resultFile is not null");
        }

        // 初始化结果文件
        File file = FileSystem.FusionResult.getResultFile(job.getJobId());
        job.getJobResult().resultFile = file;

        String header = StringUtil.joinByComma(getMyselfHeader());
        if (getPartnerHeader().size() > 0) {
            header += "," + StringUtil.joinByComma(
                    getPartnerHeader().stream()
                            .map(x -> "partner_" + x)
                            .collect(Collectors.toList())
            );
        }
        header += System.lineSeparator();

        BufferedWriter writer = FileUtil.buildBufferedWriter(file, false);
        writer.write(header);

        return writer;
    }

    private LinkedHashSet<String> getMyselfHeader() {
        if (myselfHeader == null) {
            HashConfig hashConfig = job.getMyself().dataResourceInfo.hashConfig;
            myselfHeader = hashConfig.getIdHeadersForCsv();
        }

        return myselfHeader;
    }

    private LinkedHashSet<String> getPartnerHeader() {
        if (partnerHeader == null) {
            LinkedHashSet<String> header = new LinkedHashSet<>();

            // 输出附加字段
            LinkedHashSet<String> additionalResultColumns = job.getPartner().dataResourceInfo.additionalResultColumns;
            if (additionalResultColumns != null) {
                header.addAll(additionalResultColumns);
            }
            partnerHeader = header;
        }

        return partnerHeader;
    }

    @Override
    public JobPhase getPhase() {
        return JobPhase.SaveResult;
    }

    @Override
    public long getTotalWorkload() {
        return job.getJobResult().fusionCount;
    }

    /**
     * 所有角色都需要下载结果
     */
    @Override
    protected boolean skipThisAction() {
        return false;
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.closeQuietly(this.writer);
    }
}
