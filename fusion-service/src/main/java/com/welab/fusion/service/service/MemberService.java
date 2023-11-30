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

import com.welab.fusion.service.api.member.TestConnectApi;
import com.welab.fusion.service.database.base.MySpecification;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.MemberDbModel;
import com.welab.fusion.service.database.repository.MemberRepository;
import com.welab.fusion.service.dto.entity.MemberInputModel;
import com.welab.fusion.service.dto.entity.MemberOutputModel;
import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.service.AliveApi;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/20
 */
@Service
public class MemberService extends AbstractService {
    /**
     * 为保持一致性，自己也作为一个 member 储存在数据库中，其 name 固定。
     */
    public static final String MYSELF_NAME = "myself";
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private GatewayService gatewayService;

    public MemberDbModel getMyself() throws Exception {
        MemberDbModel myself = memberRepository.findByName(MYSELF_NAME);

        if (myself == null) {
            myself = addMyself();
        }

        return myself;
    }

    public synchronized MemberDbModel addMyself() throws Exception {
        MemberDbModel myself = memberRepository.findByName(MYSELF_NAME);
        if (myself != null) {
            return myself;
        }

        FusionConfigModel config = globalConfigService.getFusionConfig();
        if (StringUtil.isEmpty(config.publicServiceBaseUrl)) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("未设置“对外服务地址”，请在全局配置中填写，供其它节点通信。");
        }

        MemberInputModel input = new MemberInputModel();
        input.setName(MYSELF_NAME);
        input.setBaseUrl(config.publicServiceBaseUrl);
        input.setPublicKey(config.publicKey);
        return save(input);
    }

    /**
     * 尝试保存合作方信息
     */
    public void trySave(FusionNodeInfo caller) throws Exception {
        if (caller == null) {
            return;
        }

        try {
            save(null, caller.baseUrl, caller.publicKey);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
    }

    public MemberDbModel save(MemberInputModel input) throws Exception {
        return save(input.getName(), input.getBaseUrl(), input.getPublicKey());
    }

    public synchronized MemberDbModel save(String name, String baseUrl, String publicKey) throws Exception {
        boolean hasName = StringUtil.isNotEmpty(name);

        // 设置默认名称
        if (!hasName) {
            name = MemberService.buildMemberId(baseUrl);
        }

        MemberDbModel model = findByUrl(baseUrl);

        // 有则更新，无则新增。
        if (model == null) {
            model = new MemberDbModel();
        }

        /**
         * 仅在以下两种情况下设置名称，避免在未指定名称时覆盖。
         * 1. 输入了名称
         * 2. 数据库中没有名称
         */
        if (hasName || StringUtil.isEmpty(model.getName())) {
            model.setName(name);
        }

        model.setBaseUrl(baseUrl);
        model.setPublicKey(publicKey);
        model.save();

        return model;
    }

    public static String buildMemberId(String baseUrl) throws URISyntaxException {
        URI uri = new URI(baseUrl);
        return uri.getHost() + ":" + uri.getPort();
    }

    public MemberDbModel findByUrl(String url) throws URISyntaxException {
        return findById(buildMemberId(url));
    }

    public MemberDbModel findById(String id) {
        if (id == null) {
            return null;
        }
        return memberRepository.findById(id).orElse(null);
    }

    public List<MemberOutputModel> list(String name) {
        MySpecification<MemberDbModel> where = Where
                .create()
                .contains("name", name)
                .build();

        List<MemberDbModel> list = memberRepository.findAll(where);
        return ModelMapper.maps(list, MemberOutputModel.class);
    }

    public void delete(String id) {
        memberRepository.deleteById(id);
    }

    /**
     * 测试双方的连通性
     * 为保障双方的连通性，需要互相请求对方。
     *
     * 以 A 为测试方，B 为被测试方举例：
     * 1. A 的前端请求自己后端的 TestConnectApi
     * 2. A 的后端请求 B 的 TestConnectApi
     * 3. B 后端请求 A 的 AliveApi（不请求 TestConnectApi 是为了避免死循环）
     * 4. 如果都成功，则测试通过
     */
    public void testConnection(MemberInputModel input) throws Exception {
        // 请求来自己方前端，发起请求访问合作方的 TestConnectApi。
        if (input.fromMyselfFrontEnd()) {
            gatewayService.callOtherFusionNode(
                    FusionNodeInfo.of(input.getPublicKey(), input.getBaseUrl()),
                    TestConnectApi.class
            );
        }
        MemberDbModel myself = getMyself();
        // 别人请求我，我请求回去。
        if (input.fromOtherFusionNode()) {
            gatewayService.callOtherFusionNode(
                    FusionNodeInfo.of(input.caller.publicKey, input.caller.baseUrl),
                    AliveApi.class,
                    MemberInputModel.of(myself.getPublicKey(), myself.getBaseUrl())
            );
        }

        // 如果能联通，自动保存。
        try {
            save(input);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            // ignore
        }
    }


    public FusionNodeInfo getPartnerFusionNodeInfo(String memberId) {
        return findById(memberId).toFusionNodeInfo();
    }
}
