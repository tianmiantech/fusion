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
package com.welab.wefe.common.web.util;

/**
 * @author zane
 * @date 2022/3/16
 */
public class AccountInfo {
    public String id;
    /**
     * 昵称
     */
    public String username;

    public static AccountInfo of(String id, String nickname) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setId(id);
        accountInfo.setUsername(nickname);
        return accountInfo;
    }

    // region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    // endregion
}
