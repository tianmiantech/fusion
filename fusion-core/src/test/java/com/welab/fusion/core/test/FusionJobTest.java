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
package com.welab.fusion.core.test;

import com.alibaba.fastjson.JSON;
import com.welab.fusion.core.Job.FusionJob;
import com.welab.fusion.core.Job.JobMember;
import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.bloom_filter.PsiBloomFilterCreator;
import com.welab.fusion.core.data_resource.base.DataResourceInfo;
import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.function.JobFunctions;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.test.function.DownloadPsiBloomFilterFunctionImpl;
import com.welab.fusion.core.test.function.SavePsiBloomFilterFunctionImpl;
import com.welab.fusion.core.util.PSIUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class FusionJobTest {
    private static String job_id = UUID.randomUUID().toString().replace("-", "");
    private static JobMember memberA;
    private static JobMember memberB;
    private static FusionJob memberAJob;
    private static FusionJob memberBJob;

    public static void main(String[] args) throws Exception {
        // 设置小一点，生成的过滤器体积也小一点，便于测试。
        PsiBloomFilterCreator.MIN_EXPECTED_INSERTIONS = 10_000;

        FileSystem.init(Paths.get("D:\\data\\wefe\\fusion"));

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
        List<HashConfig> hashConfigs = Arrays.asList(HashConfig.of(HashMethod.MD5, "id"));

        // String csv = "ivenn_10w_20210319_vert_promoter.csv";
        String csv = "promoter-569.csv";
        File file = new File("D:\\data\\wefe\\" + csv);
        CsvTableDataSourceReader readerA = new CsvTableDataSourceReader(file);
        CsvTableDataSourceReader readerB = new CsvTableDataSourceReader(file);

        DataResourceInfo dataResourceInfoA = DataResourceInfo.of("dataResourceInfoA", file.getName(), readerA.getTotalDataRowCount(), DataResourceType.TableDataSource, hashConfigs);
        DataResourceInfo dataResourceInfoB = DataResourceInfo.of("dataResourceInfoB", file.getName(), readerB.getTotalDataRowCount(), DataResourceType.TableDataSource, hashConfigs);


        memberA = JobMember.of("memberA", "memberA", dataResourceInfoA);
        memberA.tableDataResourceReader = readerA;

        memberB = JobMember.of("memberB", "memberB", dataResourceInfoB);
        memberB.tableDataResourceReader = readerB;

        memberAJob = new FusionJob(job_id, memberA, memberB, createAJobFunctions());
        memberBJob = new FusionJob(job_id, memberB, memberA, createBJobFunctions());
    }

    private static JobFunctions createAJobFunctions() {
        JobFunctions jobFunctions = createJobFunctions();
        jobFunctions.getPartnerProgressFunction = () -> memberBJob.getMyProgress();

        return jobFunctions;
    }

    private static JobFunctions createBJobFunctions() {
        JobFunctions jobFunctions = createJobFunctions();
        jobFunctions.getPartnerProgressFunction = () -> memberAJob.getMyProgress();

        return jobFunctions;
    }

    private static JobFunctions createJobFunctions() {
        JobFunctions jobFunctions = new JobFunctions();
        jobFunctions.downloadPsiBloomFilterFunction = new DownloadPsiBloomFilterFunctionImpl();
        jobFunctions.savePsiBloomFilterFunction = new SavePsiBloomFilterFunctionImpl();

        jobFunctions.encryptPsiRecordsFunction = (String memberId, String psiBloomFilterId, List<String> bucket) -> {
            Path dir = FileSystem.PsiBloomFilter.getPath(psiBloomFilterId);
            PsiBloomFilter psiBloomFilter = PsiBloomFilter.of(dir);
            return PSIUtils.encryptPsiRecords(psiBloomFilter, bucket);
        };

        jobFunctions.saveFusionResultFunction = (result) -> {
            System.out.println(JSON.toJSONString(result, true));
        };
        return jobFunctions;
    }


}
