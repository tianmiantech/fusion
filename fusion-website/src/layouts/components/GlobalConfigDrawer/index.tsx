

import {useState,useImperativeHandle,forwardRef, useEffect} from 'react'
import { Button, Drawer,Input,Spin,Form, message,Row } from 'antd'
import TmDrawer from '@/components/TmDrawer'
import { history } from '@umijs/max';
import styles from './index.less'
import { useRequest,useMount } from 'ahooks';
import { useImmer } from 'use-immer';
import { getGlobalConfig,updateGlobalConfig,testMySelfConnect } from './service';
import lodash from 'lodash'
import { testPartnerConntent } from '@/pages/job/service';
import QRCode from './QRCodeCard'

const { TextArea } = Input;
type configDataTypes = {
    public_key?:string,
    base_url?:string

}
const Index = forwardRef((props,ref)=>{
    const [formRef] = Form.useForm();
    const [visible, setVisible] = useState(false);
    const [isTestConnect,setIsTestConnect] = useState(false)
    const [configData,setConfigData] = useState<configDataTypes>({})

    useImperativeHandle(ref,()=>{
        return {
            showDrawer:()=>{  
                setVisible(true)
            }
        }
    })

    useEffect(()=>{
        if(visible){
            runGetGlobalConfig();
        }
    },[visible])

    const {run:runGetGlobalConfig,loading:getGlobalConfigLaoding} = useRequest(async ()=>{
        const res = await getGlobalConfig();
        const {code,data={}} = res;
        if(code === 0){
            const public_key = lodash.get(data,'fusion.public_key')
            const base_url = lodash.get(data,'fusion.public_service_base_url')
            formRef.setFieldsValue({public_key,base_url});
            if(base_url && public_key){
                setConfigData({
                    public_key,
                    base_url
                
                })   
            }
        }
    },{manual:true})


    const {run:runUpdateGlobalConfig,loading:updateGlobalConfigLoading} = useRequest(async ()=>{
        const values = await formRef.validateFields();
        const requestParams = {
            groups:{
                fusion:{
                    public_service_base_url:values.base_url
                }
            }
        }
        const res = await updateGlobalConfig(requestParams);
        const {code} = res;
        if(code === 0){
            message.success('保存成功');
            setConfigData({
                public_key:values.public_key,
                base_url:values.base_url
            })
        }
    
    },{manual:true})

    const {run:runTestPartnerConntention,loading:testPartnerConntentLoading} = useRequest(async ()=>{
        const values = await formRef.validateFields();
        const res = await testMySelfConnect(values.base_url);
        const {code} = res;
        if(code === 0){
            setIsTestConnect(true)
            message.success('连接成功')
        }
    },{manual:true})

    const onOk = ()=>{
        if (!isTestConnect) {
            message.warn('请先点击 连通性测试，确保对外连接地址可用')
            return
        }
        runUpdateGlobalConfig();

    }

    return <TmDrawer 
        title={'全局配置'} 
        onClose={()=>{
            setVisible(false)
        }} 
        width={500}
        open={visible}
        okText='保存'
        onOk={onOk}
        extra={<Button type="link" style={{color:'white'}} onClick={()=>{history.push('/user/add')}}>新增用户</Button>}
       >
        <Spin spinning={getGlobalConfigLaoding||updateGlobalConfigLoading||testPartnerConntentLoading}>
            <Form  layout="vertical"  form={formRef}>
                <Form.Item style={{marginBottom:0}} label={'对外服务地址'} name='base_url' rules={[{required:true}]}>
                    <Input/>
                </Form.Item>
                <Row justify="end">
                        <Button type="link"  onClick={runTestPartnerConntention}>连通性测试</Button>
                    </Row>
                <Form.Item label={'公钥 (密钥用于与其他合作方通信时进行信息加密)'} name='public_key' required >
                    <TextArea
                        disabled
                        rows={3}
                        />
                </Form.Item>
            </Form>
           {configData.base_url && configData.public_key &&  <div className={styles.resetBtnContainer}>
                <QRCode configData={configData}/>
            </div>}
        </Spin>
    </TmDrawer>
})
export default Index