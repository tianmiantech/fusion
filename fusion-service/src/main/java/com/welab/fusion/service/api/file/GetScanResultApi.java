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

package com.welab.fusion.service.api.file;


import com.welab.fusion.service.api.file.security.FileSecurityScanner;
import com.welab.fusion.service.api.file.security.ScanResult;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author zane.luo
 */
@Api(path = "file/scan_result", name = "获取文件安全扫描结果")
public class GetScanResultApi extends AbstractApi<GetScanResultApi.Input, GetScanResultApi.Output> {

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        ScanResult scanResult = FileSecurityScanner.SCAN_RESULT_MAP.get(input.scanSessionId);

        // 无结果的情况下，返回成功。
        if (scanResult == null) {
            scanResult = new ScanResult();
            scanResult.success();
        }

        return success(Output.of(scanResult));
    }

    public static class Output {
        public ScanResult scanResult;

        public static Output of(ScanResult scanResult) {
            Output output = new Output();
            output.scanResult = scanResult;
            return output;
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String scanSessionId;
    }
}
