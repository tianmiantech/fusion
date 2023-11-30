import React, { useState,forwardRef,useImperativeHandle } from 'react';
import { Form, Input, Button, Row, Col,Tooltip, Spin } from 'antd';
import { FolderOpenOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { useModel } from '@umijs/max';
import { useRequest } from "ahooks";
import {testPartnerConntent,TestPartnerConntentRequestInterface,sendJobToProvider,SendTaskToProviderRequestInterface} from '../service'

const ProviderForm = forwardRef((props, ref) => {

  const {jobFormData} = useModel('task.useJobForm')
  const [formRef] = Form.useForm();

  //测试协作方连通性
  const {run:runTestPartnerConntent,loading } = useRequest(async (params:TestPartnerConntentRequestInterface)=>{
    const reponse = await testPartnerConntent(params)

  },{manual:true})

  //发送任务到协作方
  const {run:runSendJobToProvider,loading:loadingSendJobToProvider } = useRequest(async (params:SendTaskToProviderRequestInterface)=>{
    const reponse = await sendJobToProvider(params)
    
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
    formRef.validateFields().then(async (values)=>{
      const requestParams = { ...values,job_id:jobFormData.job_id }
      runSendJobToProvider(requestParams)
    })
  };

  return (
    <>
      <Row justify="center" className="form-scroll">
        <Col lg={{span: 16}} md={{span: 24}}>
          <Spin spinning={loading||loadingSendJobToProvider}>
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
          <Button type="primary" onClick={submitData}>发起任务</Button> 
      </Row>
    </>
  );
});

export default ProviderForm;
