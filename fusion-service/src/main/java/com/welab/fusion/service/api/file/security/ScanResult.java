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
package com.welab.fusion.service.api.file.security;

/**
 * @author zane.luo
 * @date 2023/3/17
 */
public class ScanResult {
    public boolean finished;
    public boolean success;
    public String message;

    public static ScanResult of() {
        ScanResult result = new ScanResult();
        result.finished = false;
        return result;
    }

    public void success() {
        this.finished = true;
        this.success = true;
    }

    public void fail(String message) {
        this.finished = true;
        this.success = false;
        this.message = message;
    }
}
