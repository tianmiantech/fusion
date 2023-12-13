

import {useState,useImperativeHandle,forwardRef} from 'react'
import { Button, Drawer,Input,Spin,Form, message,Row } from 'antd'
import { TmDrawer } from '@tianmiantech/pro';
import styles from './index.less'
import { useRequest,useMount } from 'ahooks';
import { useImmer } from 'use-immer';
import { getGlobalConfig,updateGlobalConfig,UpdateGlobalConfigRequestInterface } from './service';
import lodash from 'lodash'
import { testPartnerConntent } from '@/pages/job/service';

const { TextArea } = Input;
const Index = forwardRef((props,ref)=>{
    const [formRef] = Form.useForm();
    const [visible, setVisible] = useState(false);
    const [okLoading,setOkLoading] = useState(false);

    useImperativeHandle(ref,()=>{
        return {
            showDrawer:()=>{  
                setVisible(true)
            }
        }
    })

    useMount(()=>{
        runGetGlobalConfig();
    })

    const {run:runGetGlobalConfig,loading:getGlobalConfigLaoding} = useRequest(async ()=>{
        const res = await getGlobalConfig();
        const {code,data={}} = res;
        if(code === 0){
            const public_key = lodash.get(data,'fusion.public_key')
            const public_service_base_url = lodash.get(data,'fusion.public_service_base_url')
            formRef.setFieldsValue({public_key,public_service_base_url});
        }
    },{manual:true})


    const {run:runUpdateGlobalConfig,loading:updateGlobalConfigLoading} = useRequest(async ()=>{
        const values = await formRef.validateFields();
        const requestParams = {
            groups:{
                fusion:{
                    public_service_base_url:values.public_service_base_url
                }
            }
        }
        const res = await updateGlobalConfig(requestParams);
        const {code} = res;
        if(code === 0){
            message.success('保存成功');
        }
    
    },{manual:true})

    const testPartnerConntention = ()=>{

    }

  

    const resetKey = ()=>{
        setOkLoading(true);
        setTimeout(()=>{
            setOkLoading(false);
        },2000)
    }

    return <TmDrawer 
        title={'全局配置'} 
        onClose={()=>{
            setVisible(false)
        }} 
        width={500}
        open={visible}
        okText='保存'
        onOk={runUpdateGlobalConfig}
        loading={getGlobalConfigLaoding||updateGlobalConfigLoading}
       >
        <Spin spinning={okLoading}>
            <Form  layout="vertical"  form={formRef}>
                <Form.Item label={'对外服务地址'} name='public_service_base_url' rules={[{required:true}]}>
                    <Input/>
                </Form.Item>
                <Form.Item>
                    <Row justify="end">
                        <Button type="link"  onClick={testPartnerConntention}>连通性测试</Button>
                    </Row>
                    </Form.Item>
                <Form.Item label={'公钥 (密钥用于与其他合作方通信时进行信息加密)'} name='public_key' required >
                    <TextArea
                        disabled
                        rows={3}
                        />
                </Form.Item>
            </Form>
            {/* <div className={styles.resetBtnContainer}>
                <Button type='link' onClick={resetKey}>重置秘钥</Button>
            </div> */}
        </Spin>
    </TmDrawer>
})
export default Index