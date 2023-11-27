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
package com.welab.fusion.service.service;

import com.welab.fusion.service.database.base.MySpecification;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.PartnerDbModel;
import com.welab.fusion.service.database.repository.PartnerRepository;
import com.welab.fusion.service.dto.entity.PartnerInputModel;
import com.welab.fusion.service.dto.entity.PartnerOutputModel;
import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/20
 */
@Service
public class PartnerService extends AbstractService {
    /**
     * 为保持一致性，自己也作为一个 partner 储存在数据库中，其 name 固定。
     */
    public static final String MYSELF_NAME = "myself";
    @Autowired
    private PartnerRepository partnerRepository;

    public PartnerDbModel getMyself() throws StatusCodeWithException {
        PartnerDbModel myself = partnerRepository.findByName(MYSELF_NAME);

        if (myself == null) {
            myself = addMyself();
        }

        return myself;
    }

    public synchronized PartnerDbModel addMyself() throws StatusCodeWithException {
        PartnerDbModel myself = partnerRepository.findByName(MYSELF_NAME);
        if (myself != null) {
            return myself;
        }

        FusionConfigModel config = globalConfigService.getFusionConfig();
        if (StringUtil.isEmpty(config.publicServiceBaseUrl)) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("未设置“对外服务地址”，请在全局配置中填写，供其它节点通信。");
        }

        PartnerInputModel input = new PartnerInputModel();
        input.setName(MYSELF_NAME);
        input.setBaseUrl(config.publicServiceBaseUrl);
        input.setPublicKey(config.publicKey);
        return save(input);
    }

    public synchronized PartnerDbModel save(PartnerInputModel input) {
        PartnerDbModel model = partnerRepository.findByName(input.getName());

        // 有则更新，无则新增。
        if (model == null) {
            model = new PartnerDbModel();
        }
        model.setName(input.getName());
        model.setBaseUrl(input.getBaseUrl());
        model.setPublicKey(input.getPublicKey());
        model.save();

        return model;
    }

    public List<PartnerOutputModel> list(String name) {
        MySpecification<PartnerDbModel> where = Where
                .create()
                .contains("name", name)
                .build();

        List<PartnerDbModel> list = partnerRepository.findAll(where);
        return ModelMapper.maps(list, PartnerOutputModel.class);
    }

}
