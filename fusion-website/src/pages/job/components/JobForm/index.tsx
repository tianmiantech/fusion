import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col,Alert,Spin, message } from 'antd';
import { dataResourceTypeMap, dataSetAddMethodMap } from '@/constant/dictionary';
import DataSetPreview from "@/components/DataSetPreview";
import HashForm from '../HashForm/index';
import DataSourceForm from '../DataSourceForm';
import BloomFilterManage from '../BloomFilterManage';
import { formRuleRequire } from '@/utils/common';
import { useImmer } from 'use-immer';
import FileChunkUpload from '@/components/FileChunkUpload'
import { useModel } from '@umijs/max';
import { CheckCircleFilled } from '@ant-design/icons';
import './index.less'
import {createJob,CreateJobRequestInterface} from '../../service'
import lodash from 'lodash'
import { useRequest } from 'ahooks';
import useDetail from "../../hooks/useDetail";


interface JobFormDataInterface {
  BFManageOpen:boolean,
  dataourceColumnList:string[],//数据预览的column列表，用来选择设置hash
  uploadFileName:string //上传到后端的文件名
}

interface UploadFinishCallBackInterface {
  uploadFileName:string,
  dataourceColumnList:string[]
}
interface JobFormPropsInterface {

}


const JobForm = forwardRef((props:JobFormPropsInterface, ref) => {

  const [formRef] = Form.useForm();
  const {detailData,setDetailData} = useDetail();

  const [jobFormData,setJobFormData] = useImmer<JobFormDataInterface>({
    BFManageOpen:false,
    uploadFileName:'',
    dataourceColumnList:[]
  })

  const {run:runCreateJob,loading:createJobloading} = useRequest(async (params:CreateJobRequestInterface)=>{
    const reponse = await createJob(params)
    const {code,data} = reponse;
    if(code === 0){
      message.success('保存成功')
      setDetailData(g=>{
        g.jobId = lodash.get(data,'job_id');
      })
    }  
  },{ manual:true})
  
  const uploadFinishCallBack = (parma:UploadFinishCallBackInterface)=>{
    const {uploadFileName,dataourceColumnList} = parma
    setJobFormData(g=>{
      g.dataourceColumnList = dataourceColumnList;
      g.uploadFileName = uploadFileName
    })
  }

  /**
   *  初始化表格填充内容
    remark:string,
    status:string,
    data_resource_type:string,
    hash_config:{},
    table_data_resource_info:{},
    dataSetAddMethod:string,
    bloom_filter_id:string,
   */
  useEffect(()=>{
    if(detailData.role==='promoter' && detailData.jobDetailData){
      const dataSetAddMethod = lodash.get(detailData,'jobDetailData.myself.table_data_resource_info.add_method');
      formRef.setFieldsValue({
        remark:detailData.jobDetailData.remark,
        status:detailData.jobDetailData.status,
        data_resource_type:detailData.jobDetailData['myself'].data_resource_type,
        hash_config:detailData.jobDetailData['myself'].hash_config,
        table_data_resource_info:detailData.jobDetailData['myself'].table_data_resource_info,
        dataSetAddMethod:dataSetAddMethod
      })
    }
  },[detailData.jobDetailData])


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
      getJobFormData:()=>{
        return jobFormData
      },
      setJobFormData
    }
  });

  const checkFormDisable = ()=>{
    return detailData.jobDetailData && detailData.jobDetailData.status!=='editing'
  }

  return <>
      <Spin spinning={createJobloading} >
      <Row justify="center" className="form-scroll">
          <Col lg={{span: 16}} md={{span: 24}}>
            <Form
              form={formRef}
              initialValues={{data_resource_type:'TableDataSource',dataSetAddMethod:'HttpUpload'}}
              layout="vertical"
              disabled={checkFormDisable()}
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
                <HashForm columnList={jobFormData.dataourceColumnList}/> 
              </Form.Item>
              <Form.Item name="remark" label="任务备注">
                <Input.TextArea rows={4} placeholder="请输入" />
              </Form.Item>
            </Form>
          </Col>
        
      </Row>
      </Spin>
      <Row className="operation-area">
        <Button loading={createJobloading} disabled={checkFormDisable()} type="primary" onClick={submitFormData}>{detailData.jobId?'更新':'保存'}</Button>
      </Row>
      {/* 布隆过滤器管理 */}
      <BloomFilterManage
        open={jobFormData.BFManageOpen}
        onClose={() => {setJobFormData(g=>{g.BFManageOpen = false})}}
      />
    </>
});

export default JobForm;
