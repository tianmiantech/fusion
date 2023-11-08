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

package com.welab.wefe.common.web.config;

import com.welab.wefe.common.web.constant.EnvName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * config.properties
 *
 * @author lonnie
 */
@Component("commonConfig")
public class CommonConfig {

    /**
     * 日志文件储存路径
     */
    @Value("${logging.file:}")
    private String loggingFilePath;

    @Value("${wefe.file.upload.dir:}")
    private String fileUploadDir;

    @Value("${env.name:prod}")
    private EnvName envName;

    @Value("${cors.allowed.origins:*}")
    private String[] corsAllowedOrigins;

    /**
     * 反动关键词列表（使用逗号分隔）
     */
    @Value("${security.reactionary.keywords:}")
    private String reactionaryKeywords;

    /**
     * 运行在iam环境中
     */
    @Value("${run.in.iam.env:false}")
    private boolean runInIamEnv;

    /**
     * 当运行在iam环境中时，要排除鉴权的接口列表；多个用英文逗号分割，同时支持通配符方式，如api*,PrivateInfor*
     */
    @Value("${in.iam.env.authentication.exclude.path:}")
    private String[] inIamEnvAuthenticationExcludePath;

    // region getter/setter

    public String getLoggingFilePath() {
        return loggingFilePath;
    }

    public void setLoggingFilePath(String loggingFilePath) {
        this.loggingFilePath = loggingFilePath;
    }

    public String getFileUploadDir() {
        return fileUploadDir;
    }

    public void setFileUploadDir(String fileUploadDir) {
        this.fileUploadDir = fileUploadDir;
    }

    public EnvName getEnvName() {
        return envName;
    }

    public void setEnvName(EnvName envName) {
        this.envName = envName;
    }

    public String[] getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String[] corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public String getReactionaryKeywords() {
        return reactionaryKeywords;
    }

    public void setReactionaryKeywords(String reactionaryKeywords) {
        this.reactionaryKeywords = reactionaryKeywords;
    }

    public boolean isRunInIamEnv() {
        return runInIamEnv;
    }

    public void setRunInIamEnv(boolean runInIamEnv) {
        this.runInIamEnv = runInIamEnv;
    }

    public String[] getInIamEnvAuthenticationExcludePath() {
        return inIamEnvAuthenticationExcludePath;
    }

    public void setInIamEnvAuthenticationExcludePath(String[] inIamEnvAuthenticationExcludePath) {
        this.inIamEnvAuthenticationExcludePath = inIamEnvAuthenticationExcludePath;
    }


    // endregion

}
