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

package com.welab.fusion.service;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hunter.zhao
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(
        lazyInit = true,
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {
                FusionService.class,
                Launcher.class
        }
)
public class FusionService implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(FusionService.class);

    public static void main(String[] args) {
        Launcher
                .instance()
                //.apiLogger(new BoardApiLogger())
                .apiPackageClass(FusionService.class)
                .launch(FusionService.class, args);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }

}
