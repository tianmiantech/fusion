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
package com.welab.fusion.core.test.ecdh_psi;

import com.alibaba.fastjson.JSON;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.EcdhPsiJob;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.EcdhPsiJobFunctions;
import com.welab.fusion.core.Job.algorithm.ecdh_psi.EcdhPsiJobMember;
import com.welab.fusion.core.Job.data_resource.DataResourceInfo;
import com.welab.fusion.core.Job.data_resource.DataResourceType;
import com.welab.fusion.core.io.data_source.CsvTableDataSourceReader;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.core.hash.HashConfigItem;
import com.welab.fusion.core.hash.HashMethod;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.core.test.function.DownloadPartnerFileFunctionImpl;

import java.io.File;
import java.util.UUID;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class EcdhPsiJobTest {
    private static String job_id = UUID.randomUUID().toString().replace("-", "");
    private static EcdhPsiJobMember memberA;
    private static EcdhPsiJobMember memberB;
    private static EcdhPsiJob memberAJob;
    private static EcdhPsiJob memberBJob;

    public static void main(String[] args) throws Exception {
        FileSystem.init("D:\\data\\fusion");

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


        memberA = EcdhPsiJobMember.of("memberA", "memberA", dataResourceInfoA);
        memberA.tableDataResourceReader = readerA;

        memberB = EcdhPsiJobMember.of("memberB", "memberB", dataResourceInfoB);
        memberB.tableDataResourceReader = readerB;

        memberAJob = new EcdhPsiJob(job_id, memberA, memberB, createAJobFunctions());
        memberBJob = new EcdhPsiJob(job_id, memberB, memberA, createBJobFunctions());
    }

    private static EcdhPsiJobFunctions createAJobFunctions() {
        EcdhPsiJobFunctions jobFunctions = createJobFunctions();
        jobFunctions.getPartnerProgressFunction = (jobId) -> memberBJob.getMyProgress();

        return jobFunctions;
    }

    private static EcdhPsiJobFunctions createBJobFunctions() {
        EcdhPsiJobFunctions jobFunctions = createJobFunctions();
        jobFunctions.getPartnerProgressFunction = (jobId) -> memberAJob.getMyProgress();

        return jobFunctions;
    }

    private static EcdhPsiJobFunctions createJobFunctions() {
        EcdhPsiJobFunctions jobFunctions = new EcdhPsiJobFunctions();
        jobFunctions.downloadPartnerFileFunction = new DownloadPartnerFileFunctionImpl();
        jobFunctions.finishJobFunction = (jobId, myRole) -> {
            System.out.println("finishJobFunction");
        };

        jobFunctions.saveFusionResultFunction = (jobId, myRole, result, totalSizeConsumer, downloadSizeConsumer) -> {
            System.out.println(JSON.toJSONString(result, true));
        };
        return jobFunctions;
    }


}
