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

package com.welab.fusion.service.api.data_source;

import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.Collection;

/**
 * @author zane.luo
 */
@Api(path = "data_source/available_type", name = "列举支持的数据库类型")
public class AvailableTypeApi extends AbstractNoneInputApi<AvailableTypeApi.Output> {

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        return success(Output.of(DatabaseType.LOCAL_SUPPORT_DATABASE_TYPES));
    }

    public static class Output {
        public Collection<DatabaseType> availableType;

        public static Output of(Collection<DatabaseType> availableType) {
            Output output = new Output();
            output.availableType = availableType;
            return output;
        }
    }
}