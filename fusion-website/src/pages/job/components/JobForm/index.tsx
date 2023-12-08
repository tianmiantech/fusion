import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col,Alert,Spin, message } from 'antd';
import { dataResourceTypeMap, dataSetAddMethodMap,JOB_STATUS } from '@/constant/dictionary';
import HashForm from '../HashForm/index';
import DataSourceForm from '../DataSourceForm';
import BloomFilterManage from '../BloomFilterManage';
import { formRuleRequire } from '@/utils/common';
import { useImmer } from 'use-immer';
import FileChunkUpload from '@/components/FileChunkUpload'
import './index.less'
import lodash from 'lodash'
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
  loading?:boolean,
  renderFormAction?:()=>React.ReactNode, //表单提交按钮，发起方，协作方不一样，交给各组装
}


const JobForm = forwardRef((props:JobFormPropsInterface, ref) => {
  const { loading=false,renderFormAction} = props
  const [formRef] = Form.useForm();
  const {detailData,setDetailData} = useDetail();

  const [jobFormData,setJobFormData] = useImmer<JobFormDataInterface>({
    BFManageOpen:false,
    uploadFileName:'',
    dataourceColumnList:[]
  })

 
  
  const uploadFinishCallBack = (parma:UploadFinishCallBackInterface)=>{
    const {uploadFileName,dataourceColumnList} = parma
    setJobFormData(g=>{
      g.dataourceColumnList = dataourceColumnList;
      g.uploadFileName = uploadFileName
    })
  }
 
  useImperativeHandle(ref, () => {
    return {
      validateFields:formRef.validateFields,
      setFieldsValue:formRef.setFieldsValue,
    }
  });

  const checkFormDisable = ()=>{
    const status = lodash.get(detailData,'jobDetailData.status','')
    if(!status || status === JOB_STATUS.EDITING){
      return false
    }
    return true
  }

  return <>
      <Spin spinning={loading} >
      <Row justify="center" className="form-scroll">
          <Col lg={{span: 16}} md={{span: 24}}>
            <Form
              form={formRef}
              initialValues={{data_resource_type:'TableDataSource',dataSetAddMethod:'HttpUpload'}}
              layout="vertical"
              disabled={checkFormDisable()}
            >
              <Form.Item style={{marginBottom:0}}  label="样本类型" required>
                <Form.Item name="data_resource_type" style={{ display: 'inline-block', marginBottom: 0 }} rules={[{ required: true }]}>
                  <Radio.Group>
                    {[...dataResourceTypeMap].map(([value, label]) => (
                      <Radio.Button key={value} value={value}>
                        {label}
                      </Radio.Button>
                    ))}
                  </Radio.Group>
                </Form.Item>
                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.data_resource_type !== cur.data_resource_type|| prev.dataSetAddMethod !== cur.dataSetAddMethod  }>
                  {({ getFieldValue }) => {
                      const data_resource_type = getFieldValue('data_resource_type');
                      const dataSetAddMethod = getFieldValue('dataSetAddMethod');
                      if(data_resource_type === 'TableDataSource') {
                        return <>
                           <Form.Item style={{marginTop:30}} name="dataSetAddMethod" label="选择样本" rules={[formRuleRequire()]}>
                            <Radio.Group>
                              {[...dataSetAddMethodMap].map(([value, label]) => (
                                <Radio key={value} value={value}>
                                  {label}
                                </Radio>
                              ))}
                            </Radio.Group>
                         </Form.Item>
                        <Form.Item >
                            {dataSetAddMethod === 'HttpUpload' ?
                              <>
                                <Form.Item name={'table_data_resource_info'}>
                                  <FileChunkUpload uploadFinishCallBack={uploadFinishCallBack}  disabled={checkFormDisable()}/>
                                </Form.Item>
                              </> :
                              <DataSourceForm formRef={formRef}/>
                            }
                        </Form.Item>
                        </>
                      } else {
                       return  <Form.Item style={{marginTop:30}} name="dataSetAddMethod" label="选择布隆过滤器" rules={[formRuleRequire()]}>
                            <Button
                              onClick={()=>{}}
                            >选择布隆过滤器</Button> 
                        </Form.Item>
                      }
                    }
                  }
                </Form.Item>
              </Form.Item>
             
              <Form.Item name={'hash_config'}>
                <HashForm disabled={checkFormDisable()} columnList={jobFormData.dataourceColumnList}/> 
              </Form.Item>
              <Form.Item name="remark" label="任务备注">
                <Input.TextArea rows={4} placeholder="请输入" />
              </Form.Item>
            </Form>
          </Col>
        
      </Row>
      </Spin>
      <Row className="operation-area">
       {renderFormAction && renderFormAction()}
      </Row>
      {/* 布隆过滤器管理 */}
      <BloomFilterManage
        open={jobFormData.BFManageOpen}
        onClose={() => {setJobFormData(g=>{g.BFManageOpen = false})}}
      />
    </>
});

export default JobForm;
