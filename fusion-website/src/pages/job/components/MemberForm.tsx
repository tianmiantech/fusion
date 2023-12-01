import React, { useState,forwardRef,useImperativeHandle } from 'react';
import { Form, Input, Button, Row, Col,Tooltip, Spin, message } from 'antd';
import { FolderOpenOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { useRequest } from "ahooks";
import {testPartnerConntent,TestPartnerConntentRequestInterface,sendJobToProvider,SendTaskToProviderRequestInterface} from '../service'

const MemberForm = forwardRef((props, ref) => {

  const [isTestConnect,setIsTestConnect] = useState(false)

  const {jobFormData} = useModel('job.useJobForm')
  const [formRef] = Form.useForm();

  //测试协作方连通性
  const {run:runTestPartnerConntent,loading:testPartnerConntentLoading } = useRequest(async (params:TestPartnerConntentRequestInterface)=>{
    const reponse = await testPartnerConntent(params)
    const {code} = reponse
    if(code === 0){
      setIsTestConnect(true)
      message.success('连接成功')
    }
  },{manual:true})



  //发送任务到协作方
  const {run:runSendJobToProvider,loading:loadingSendJobToProvider } = useRequest(async (params:SendTaskToProviderRequestInterface)=>{
    const reponse = await sendJobToProvider(params)
    const {code} = reponse;
    if(code === 0) {
      message.success('发送成功')
      setTimeout(()=>{
        history.back();
      },800)
    }
  },{manual:true})



  const testPartnerConntention = ()=>{
   
    formRef.validateFields().then(async (values)=>{
      runTestPartnerConntent(values)
    })
  }

  useImperativeHandle(ref,()=>({
    submitData
  }))

  const submitData =()=>{
    if(!isTestConnect){
      message.warn('请测试连通性')
      return
    }
    formRef.validateFields().then(async (values)=>{
      const requestParams = { ...values,job_id:jobFormData.job_id }
      runSendJobToProvider(requestParams)
    })
  };

  return (
    <>
      <Row justify="center" className="form-scroll">
        <Col lg={{span: 16}} md={{span: 24}}>
          <Spin spinning={testPartnerConntentLoading||loadingSendJobToProvider}>
          <Form
            form={formRef}
            layout="vertical"
          >
            <Form.Item name="name" label="协作方名称">
              <Input placeholder='请输入' />
            </Form.Item>
            <Form.Item label={
          <>
            <Tooltip
              placement="top"
              title={'示例：https://host:port/fusion'}
              overlayStyle={{ maxWidth: 350 }}
            >
              服务地址&nbsp;<QuestionCircleOutlined />
            </Tooltip>
          </>
          }  name="base_url" rules={[{required:true}]}>
              <Input placeholder='请输入' />
            </Form.Item>
            <Form.Item name="public_key" label="公钥"  rules={[{required:true}]}>
              <Input.TextArea placeholder='请输入' rows={4} />
            </Form.Item>
            <Form.Item>
              <Row justify="end">
                <Button type="link"  onClick={testPartnerConntention}>连通性测试</Button>
              </Row>
            </Form.Item>
          </Form>
          </Spin>
        </Col>
      </Row>
      <Row className="operation-area">
          <Button type="primary" disabled={testPartnerConntentLoading||loadingSendJobToProvider} onClick={submitData}>发起任务</Button> 
      </Row>
    </>
  );
});

export default MemberForm;
