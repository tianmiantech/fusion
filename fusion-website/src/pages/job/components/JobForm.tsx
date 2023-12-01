import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col,Alert,Spin } from 'antd';
import { dataResourceTypeMap, dataSetAddMethodMap } from '@/constant/dictionary';
import DataSetPreview from "@/components/DataSetPreview";
import HashForm from './HashForm/index';
import DataSourceForm from './DataSourceForm';
import BloomFilterManage from './BloomFilterManage';
import { formRuleRequire } from '@/utils/common';
import { useImmer } from 'use-immer';
import FileChunkUpload from '@/components/FileChunkUpload'
import { useModel } from '@umijs/max';
import { CheckCircleFilled } from '@ant-design/icons';
import '../index.less'

interface DataInterface {
  BFManageOpen:boolean,
  dataourceColumnList:string[],//数据预览的column列表，用来选择设置hash
  uploadFileName:string //上传到后端的文件名
}

interface UploadFinishCallBackInterface {
  uploadFileName:string,
  dataourceColumnList:string[]
}

const JobForm = forwardRef((props, ref) => {

  const {runCreateJob,createJobloading,jobFormData} = useModel('job.useJobForm')


  const [formRef] = Form.useForm();


  const [datas,setDatas] = useImmer<DataInterface>({
    BFManageOpen:false,
    uploadFileName:'',
    dataourceColumnList:[]

  })
  
  const uploadFinishCallBack = (parma:UploadFinishCallBackInterface)=>{
    const {uploadFileName,dataourceColumnList} = parma
    setDatas(g=>{
      g.dataourceColumnList = dataourceColumnList;
      g.uploadFileName = uploadFileName
    })
  }

  useEffect(()=>{
    console.log('jobFormData.initialValues',jobFormData.initialValues);
    formRef.setFieldsValue({...jobFormData.initialValues})
  },[jobFormData.initialValues])


  

  const submitFormData = async () => {
    const {data_resource_type,dataSetAddMethod,hash_config,remark,table_data_resource_info} = await formRef.validateFields();
    const requestParams = {
      remark,
      data_resource:{
        data_resource_type,
        table_data_resource_info,
        hash_config
      }
    }
    runCreateJob(requestParams)
  }

  useImperativeHandle(ref, () => {
    return {
      submitFormData
    }
  });

  return <>
      <Spin spinning={createJobloading} >
      <Row justify="center" className="form-scroll">
      
          <Col lg={{span: 16}} md={{span: 24}}>
            <Form
              form={formRef}
              initialValues={{data_resource_type:'TableDataSource',dataSetAddMethod:'HttpUpload'}}
              layout="vertical"
              disabled={jobFormData.job_id?true:false}
            >
              <Form.Item label="样本类型" required>
                <Form.Item name="data_resource_type" style={{ display: 'inline-block', marginBottom: 0 }} rules={[{ required: true }]}>
                  <Radio.Group>
                    {[...dataResourceTypeMap].map(([value, label]) => (
                      <Radio.Button key={value} value={value}>
                        {label}
                      </Radio.Button>
                    ))}
                  </Radio.Group>
                </Form.Item>
                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.data_resource_type !== cur.data_resource_type }>
                  {({ getFieldValue }) => {
                      return getFieldValue('data_resource_type') === 'PsiBloomFilter' ?
                      <Button
                        style={{ marginLeft: 15 }}
                        onClick={()=>{}}
                      >布隆过滤器管理</Button> : null
                    }
                  }
                </Form.Item>
              </Form.Item>
              <Form.Item name="dataSetAddMethod" label="选择样本" rules={[formRuleRequire()]}>
                <Radio.Group>
                  {[...dataSetAddMethodMap].map(([value, label]) => (
                    <Radio key={value} value={value}>
                      {label}
                    </Radio>
                  ))}
                </Radio.Group>
              </Form.Item>
              <Form.Item >
                <Form.Item  noStyle shouldUpdate={(prev, cur) => prev.dataSetAddMethod !== cur.dataSetAddMethod }>
                  {({ getFieldValue }) => 
                    getFieldValue('dataSetAddMethod') === 'HttpUpload' ?
                    <>
                      <Form.Item name={'table_data_resource_info'}>
                        <FileChunkUpload uploadFinishCallBack={uploadFinishCallBack}/>
                      </Form.Item>
                    </> :
                    <DataSourceForm formRef={formRef}/>
                  }
                </Form.Item>
              </Form.Item>
              <Form.Item name={'hash_config'}>
                <HashForm columnList={datas.dataourceColumnList}/> 
              </Form.Item>
              <Form.Item name="remark" label="任务备注">
                <Input.TextArea rows={4} placeholder="请输入" />
              </Form.Item>
            </Form>
          </Col>
        
      </Row>
      </Spin>
      <Row className="operation-area">
        {
          jobFormData.job_id ?
          <>
            <CheckCircleFilled style={{ fontSize: 24, color: '#52c41a' }} />
            <span>已保存，待发起任务</span>
          </> :
          <Button loading={createJobloading} type="primary" onClick={submitFormData}>保存</Button>
        }
      
      </Row>
      {/* 布隆过滤器管理 */}
      <BloomFilterManage
        open={datas.BFManageOpen}
        onClose={() => {setDatas(g=>{g.BFManageOpen = false})}}
      
      />
    </>
});

export default JobForm;
