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

        return save(MYSELF_NAME, config.publicServiceBaseUrl, config.publicKey);
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
        // 如果输入的是自己，不保存。
        FusionConfigModel config = globalConfigService.getFusionConfig();
        if (config != null && StringUtil.isNotEmpty(config.publicServiceBaseUrl)) {
            String myselfId = MemberService.buildMemberId(config.publicServiceBaseUrl);
            String inputId = MemberService.buildMemberId(input.getBaseUrl());
            if (inputId.equals(myselfId)) {
                return null;
            }
        }

        return save(input.getMember_name(), input.getBaseUrl(), input.getPublicKey());
    }

    public synchronized MemberDbModel save(String name, String baseUrl, String publicKey) throws Exception {
        boolean hasName = StringUtil.isNotEmpty(name);

        // 设置默认名称
        if (!hasName) {
            name = MemberService.buildMemberId(baseUrl);
        }

        String memberId = buildMemberId(baseUrl);
        MemberDbModel model = MYSELF_NAME.equals(name)
                ? memberRepository.findByName(MYSELF_NAME)
                : findById(memberId);

        // 有则更新，无则新增。
        if (model == null) {
            model = new MemberDbModel();
            model.setId(
                    MYSELF_NAME.equals(name)
                            // myself 的 id 固定，不允许修改。
                            ? MYSELF_NAME
                            : memberId
            );
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

    public static String buildMemberId(String baseUrl) {
        URI uri = null;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        int port = uri.getPort();
        String path = StringUtil.trim(uri.getPath(), '/');
        return uri.getHost()
                + (port > 0 ? ":" + port : "")
                // 必须要拼 path，不然会导致不同的服务，但是端口相同的情况下，会被认为是同一个服务。
                + (StringUtil.isEmpty(path) ? "" : "/" + path);
    }

    public static void main(String[] args) {
        System.out.println(buildMemberId("https://xbd-dev.tianmiantech.com/fusion-01/"));
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
                .notEqual("name", MYSELF_NAME)
                .contains("name", name)
                .build();

        List<MemberDbModel> list = memberRepository.findAll(where);
        return ModelMapper.maps(list, MemberOutputModel.class);
    }

    public void delete(String id) {
        if (MYSELF_NAME.equals(id)) {
            throw new RuntimeException("不能删除自己");
        }
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
        if (input.isRequestFromMyself()) {
            MemberDbModel myself = getMyself();
            gatewayService.callOtherFusionNode(
                    FusionNodeInfo.of(input.getPublicKey(), input.getBaseUrl()),
                    TestConnectApi.class,
                    MemberInputModel.of(myself.getPublicKey(), myself.getBaseUrl())
            );
        }

        // 别人请求我，我请求回去。
        if (input.isRequestFromPartner()) {
            gatewayService.callOtherFusionNode(
                    FusionNodeInfo.of(input.caller.publicKey, input.caller.baseUrl),
                    AliveApi.class
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
