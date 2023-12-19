import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col,Alert,Spin, message } from 'antd';
import { dataResourceTypeMap, dataSetAddMethodMap,JOB_STATUS, ROLE_TYPE } from '@/constant/dictionary';
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
}

interface JobFormPropsInterface {
  loading?:boolean,
  renderFormAction?:()=>React.ReactNode, //表单提交按钮，发起方，协作方不一样，交给各组装
}


const JobForm = forwardRef((props:JobFormPropsInterface, ref) => {
  const { loading=false,renderFormAction} = props
  const [formRef] = Form.useForm();
  const {detailData} = useDetail();

  const [jobFormData,setJobFormData] = useImmer<JobFormDataInterface>({
    BFManageOpen:false,
    dataourceColumnList:[]
  })

 
  
  const prevColumnsChangeCallBack = (parma:string[])=>{
    setJobFormData(g=>{
      g.dataourceColumnList = parma;
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
    const role = lodash.get(detailData,'jobDetailData.role','')
    if(!status || status === JOB_STATUS.EDITING ||(role === ROLE_TYPE.PROVIDER && status === JOB_STATUS.AUDITING) ){
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
              initialValues={{data_resource_type:'TableDataSource',add_method:'HttpUpload'}}
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
                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.data_resource_type !== cur.data_resource_type|| prev.add_method !== cur.add_method  }>
                  {({ getFieldValue }) => {
                      const data_resource_type = getFieldValue('data_resource_type');
                      const add_method = getFieldValue('add_method');
                      // 选择数据集
                      if(data_resource_type === 'TableDataSource') {
                        return <>
                           <Form.Item style={{marginTop:30}} name="add_method" label="选择样本" rules={[formRuleRequire()]}>
                            <Radio.Group>
                              {[...dataSetAddMethodMap].map(([value, label]) => (
                                <Radio key={value} value={value}>
                                  {label}
                                </Radio>
                              ))}
                            </Radio.Group>
                         </Form.Item>
                         <Form.Item name={'table_data_resource_info'}>
                         {add_method === 'HttpUpload'?<FileChunkUpload uploadFinishCallBack={prevColumnsChangeCallBack}  disabled={checkFormDisable()}/>:
                         <DataSourceForm prevColumnsChangeCallBack={prevColumnsChangeCallBack} disabled={checkFormDisable()}/>
                          }
                         </Form.Item>
                        </>
                      } else {
                       return  <Form.Item style={{marginTop:30}} name="xxxxx" label="选择布隆过滤器" rules={[formRuleRequire()]}>
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
