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

package com.welab.wefe.common.web.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.welab.wefe.common.util.StringUtil;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 当前登录用户工具类
 */
public class CurrentAccount {

    /**
     * token : AccountInfo
     */
    private static ExpiringMap<String, AccountInfo> ACCOUNT_MAP_BY_TOKEN = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(60, TimeUnit.MINUTES)
            .build();

    /**
     * 由于初始化 ExpiringMap 较慢，可以在启动时调用此方法，提前初始化。
     */
    public static void init() {
    }

    /**
     * 当前登录用户
     */
    private static final ThreadLocal<String> tokens = new TransmittableThreadLocal<>();

    /**
     * 登录之后登记登录状态
     */
    public synchronized static void logined(String accountId, String username) {
        synchronized (ACCOUNT_MAP_BY_TOKEN) {
            ACCOUNT_MAP_BY_TOKEN.entrySet().removeIf(item -> accountId.equals(item.getValue().getId()));
        }
        String token = generateToken();
        ACCOUNT_MAP_BY_TOKEN.put(token, AccountInfo.of(accountId, username));
    }

    public synchronized static void logout() {
        String token = token();
        logout(token);
    }

    public static void logout(String id) {
        synchronized (ACCOUNT_MAP_BY_TOKEN) {
            ACCOUNT_MAP_BY_TOKEN.entrySet().removeIf(item -> id.equals(item.getValue().getId()));
        }
    }

    /**
     * 从 header 中取出 token，并储存到当前线程中，使得接口内可以使用 CurrentAccount 获取当前用户信息。
     */
    public static void token(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("token");
        if (StringUtil.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
        }

        if (StrUtil.isEmpty(token)) {
            token = "";
        }
        tokens.set(token);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static AccountInfo get() {
        String token = tokens.get();
        if (token == null) {
            return null;
        }

        return ACCOUNT_MAP_BY_TOKEN.get(token);
    }

    /**
     * 当线程结束时，清除当前线程的 token
     */
    public static void leave() {
        tokens.remove();
    }

    // region getter

    public static String id() {
        AccountInfo info = get();
        if (info == null) {
            return null;
        }
        return info.id;
    }

    public static String username() {
        AccountInfo info = get();
        if (info == null) {
            return null;
        }
        return info.username;
    }

    public static String token() {
        return tokens.get();
    }

    // endregion

}
