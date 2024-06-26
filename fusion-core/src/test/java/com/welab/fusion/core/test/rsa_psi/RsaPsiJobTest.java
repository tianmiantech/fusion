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
package com.welab.fusion.core.test.rsa_psi;

import com.alibaba.fastjson.JSON;
import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.core.Job.data_resource.DataResourceInfo;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJob;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJobFunctions;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJobMember;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.test.function.DownloadPartnerFileFunctionImpl;
import com.welab.fusion.core.test.function.SaveMyPsiBloomFilterFunctionImpl;
import com.welab.fusion.core.util.PsiUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class RsaPsiJobTest {
    private static String job_id = UUID.randomUUID().toString().replace("-", "");
    private static RsaPsiJobMember memberA;
    private static RsaPsiJobMember memberB;
    private static AbstractPsiJob memberAJob;
    private static AbstractPsiJob memberBJob;

    public static void main(String[] args) throws Exception {
        // 设置小一点，生成的过滤器体积也小一点，便于测试。
        PsiBloomFilterCreator.MIN_EXPECTED_INSERTIONS = 10_000;

        FileSystem.init("D:\\data\\wefe\\fusion");

        createJobs();

        memberAJob.start();
        memberBJob.start();

        memberAJob.waitFinish();
        memberBJob.waitFinish();

        memberAJob.getMyProgress().print();
        memberBJob.getMyProgress().print();

        memberAJob.close();
        memberBJob.close();
    }

    private static void createJobs() throws Exception {
        HashConfig hashConfig = HashConfig.of(HashConfigItem.of(HashMethod.MD5, "id"));

        // String csv = "ivenn_10w_20210319_vert_promoter.csv";
        String csv = "promoter-569.csv";
        File file = new File("D:\\data\\wefe\\" + csv);
        CsvTableDataSourceReader readerA = new CsvTableDataSourceReader(file);
        CsvTableDataSourceReader readerB = new CsvTableDataSourceReader(file);

        DataResourceInfo dataResourceInfoA = DataResourceInfo.of(DataResourceType.TableDataSource, readerA.getTotalDataRowCount(), hashConfig, null);
        DataResourceInfo dataResourceInfoB = DataResourceInfo.of(DataResourceType.TableDataSource, readerB.getTotalDataRowCount(), hashConfig, null);


        memberA = RsaPsiJobMember.of(true, "memberA", "memberA", dataResourceInfoA);
        memberA.tableDataResourceReader = readerA;

        memberB = RsaPsiJobMember.of(false, "memberB", "memberB", dataResourceInfoB);
        memberB.tableDataResourceReader = readerB;

        memberAJob = new RsaPsiJob(job_id, memberA, memberB, createAJobFunctions());
        memberBJob = new RsaPsiJob(job_id, memberB, memberA, createBJobFunctions());
    }

    private static RsaPsiJobFunctions createAJobFunctions() {
        RsaPsiJobFunctions jobFunctions = createJobFunctions();
        jobFunctions.getPartnerProgressFunction = (jobId) -> memberBJob.getMyProgress();

        return jobFunctions;
    }

    private static RsaPsiJobFunctions createBJobFunctions() {
        RsaPsiJobFunctions jobFunctions = createJobFunctions();
        jobFunctions.getPartnerProgressFunction = (jobId) -> memberAJob.getMyProgress();

        return jobFunctions;
    }

    private static RsaPsiJobFunctions createJobFunctions() {
        RsaPsiJobFunctions jobFunctions = new RsaPsiJobFunctions();
        jobFunctions.downloadPartnerFileFunction = new DownloadPartnerFileFunctionImpl();
        jobFunctions.saveMyPsiBloomFilterFunction = new SaveMyPsiBloomFilterFunctionImpl();

        jobFunctions.encryptRsaPsiRecordsFunction = (String memberId, String psiBloomFilterId, List<String> bucket) -> {
            Path dir = FileSystem.PsiBloomFilter.getDir(psiBloomFilterId);
            PsiBloomFilter psiBloomFilter = PsiBloomFilter.of(dir);
            return PsiUtils.encryptPsiRecords(psiBloomFilter, bucket);
        };

        jobFunctions.saveFusionResultFunction = (jobId, myRole, result, totalSizeConsumer, downloadSizeConsumer) -> {
            System.out.println(JSON.toJSONString(result, true));
        };

        jobFunctions.finishJobFunction = (jobId, progress) -> {
            System.out.println("finish job:" + jobId);
        };
        return jobFunctions;
    }


}
