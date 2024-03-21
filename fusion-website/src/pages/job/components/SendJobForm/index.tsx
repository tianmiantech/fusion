import React, { useState,forwardRef,useImperativeHandle,useEffect } from 'react';
import { Form, Input, Button, Row, Col,Tooltip, Spin, message,Alert,Upload,Space } from 'antd';
import { UploadOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { history } from '@umijs/max';
import { useRequest } from "ahooks";
import {testPartnerConntent,TestPartnerConntentRequestInterface,sendJobToProvider,SendTaskToProviderRequestInterface} from '../../service'
import type {UploadFile,UploadProps} from 'antd'
import useDetail from '../../hooks/useDetail';
import lodash from 'lodash'
import { JOB_STATUS } from '@/constant/dictionary';
import { formRuleRequire } from '@/utils/common';
import styles from './index.less'
import jsQR from 'jsqr'
import type{  } from 'antd';
import { RcFile } from 'antd/es/upload/interface';
import { IsEmptyObject } from '@/utils/utils';

interface SendJobFormPropsInterface {
  showActionButton?:boolean
}
const SendJobForm = forwardRef((props:SendJobFormPropsInterface, ref) => {
  const { showActionButton=true } = props
  const [isTestConnect,setIsTestConnect] = useState(false)

  const [formRef] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const {detailData,clearDetailData} = useDetail()

  useEffect(()=>{
    if(detailData.jobDetailData){
      formRef.setFieldsValue({
        member_name: lodash.get(detailData,'jobDetailData.partner.member_name',''),
        base_url:lodash.get(detailData,'jobDetailData.partner.base_url',''),
        public_key:lodash.get(detailData,'jobDetailData.partner.public_key','')
      })
    }
  },[detailData.jobDetailData])

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
      clearDetailData()
      setTimeout(()=>{
        history.push('/home')
      },800)
    }
  },{manual:true})


  //测试连通性
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
      const requestParams = { ...values,job_id:detailData.jobId }
      runSendJobToProvider(requestParams)
    })
  };
  const checkFormDisable = ()=>{
    return  !(!detailData.jobDetailData || detailData.jobDetailData.status===JOB_STATUS.EDITING)
  }

  const renderRejectReason = ()=>{
    const status = lodash.get(detailData,'jobDetailData.status')
    const message = lodash.get(detailData,'jobDetailData.message')
    if(status===JOB_STATUS.DISAGREE && message){
      return <Alert message={message} type="error" className={styles.alertContainer} ></Alert>
    }
  }

  const handleChange: UploadProps['onChange'] = ({file:newFile, fileList: newFileList }) =>
    setFileList(newFileList);

  const scanQRCode = ()=>{
    if(fileList.length>0){
      const file = fileList[0]
      const reader = new FileReader();
      reader.onloadend = function(e:any) {
        const img = new Image();
        img.src = e.target.result;
        img.onload = function() {
          const canvasElement = document.createElement('canvas') as HTMLCanvasElement;
          if(canvasElement){
            const ctx = canvasElement.getContext('2d') as CanvasRenderingContext2D ;
            canvasElement.width = img.width;
            canvasElement.height = img.height;
            ctx.drawImage(img, 0, 0, img.width, img.height);
            const imageData = ctx.getImageData(0, 0, img.width, img.height);
            const code = jsQR(imageData.data, imageData.width, imageData.height);
            const data = lodash.get(code,'data','{}')
            const dataObj = JSON.parse(data)
            const {base_url,public_key} = dataObj
            if(IsEmptyObject(dataObj) || (!base_url && !public_key)){
              message.warn('未识别到有效信息')
            } else {
              formRef.setFieldsValue({base_url,public_key})
            }
            
          }
        }
      }
      if(file.originFileObj)
        reader.readAsDataURL(file.originFileObj as Blob);
    }
  }

  const renderQRScan = ()=>{
    return <div className={styles.qrContainer}>
        {fileList.length>0?<Button onClick={()=>{scanQRCode()}}>识别二维码</Button>:null}
      <Upload listType="picture" maxCount={1} fileList={fileList} onChange={handleChange} accept='image/*'>
          {fileList.length>0?null:<Button icon={<UploadOutlined />}>上传合作方二维码自动识别合作方信息</Button>}
      </Upload>
     
    </div>
  }

  return (
    <>
          <Spin spinning={testPartnerConntentLoading||loadingSendJobToProvider}>
            {renderRejectReason()}
            {renderQRScan()}
          <Form
            form={formRef}
            layout="vertical"
            disabled={checkFormDisable()}
          >
            <Form.Item name="member_name" label="协作方名称">
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
          }  name="base_url" rules={[formRuleRequire('请输入服务地址')]}>
              <Input placeholder='请输入' />
            </Form.Item>
            <Form.Item name="public_key" label="公钥"  rules={[formRuleRequire()]}>
              <Input.TextArea placeholder='请输入' rows={4} />
            </Form.Item>
            <Form.Item>
              <Row justify="end">
                <Button type="link"  onClick={testPartnerConntention}>连通性测试</Button>
              </Row>
            </Form.Item>
          </Form>
          </Spin>
      {
       showActionButton &&  <Row className="operation-area">
        <Button type="primary" disabled={testPartnerConntentLoading||loadingSendJobToProvider || checkFormDisable()} onClick={submitData}>发起任务</Button>
        </Row>
      }
      
    </>
  );
});

export default SendJobForm;
