import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col,Alert,Spin, message, Select } from 'antd';
import { dataResourceTypeMap, dataSetAddMethodMap,JOB_STATUS, ROLE_TYPE,DATARESOURCE_TYPE } from '@/constant/dictionary';
import HashForm from '../HashForm/index';
import DataSourceForm from '../DataSourceForm';
import { formRuleRequire } from '@/utils/common';
import { useImmer } from 'use-immer';
import FileChunkUpload from '@/components/FileChunkUpload'
import './index.less'
import lodash from 'lodash'
import useDetail from "../../hooks/useDetail";
import BloomFilterFormItem from '../BloomFilterFormItem';
import { useRequest,useUnmount } from 'ahooks';
import {getAlgorithmList} from '../../service'
import {IsEmptyObject} from '@/utils/utils';



interface JobFormDataInterface {
  BFManageOpen:boolean,
  dataourceColumnList:string[],//数据预览的column列表，用来选择设置hash
  algorithmList:string[],//算法列表
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
    dataourceColumnList:[],
    algorithmList:[]
  })

  // 获取算法列表 不使用manual 表示进入进入页面就请求
  const {run:runGetAlgorithmList} = useRequest(async ()=>{
    const response = await getAlgorithmList()
    const {code,data} = response;
    if(code === 0){
      const tmpList = lodash.get(data,'list',[])
      setJobFormData(g=>{
        g.algorithmList = tmpList;
      })
    }
  })

  useUnmount(()=>{
    formRef.resetFields();
  })

 
  
  const prevColumnsChangeCallBack = (parma:string[])=>{
    setJobFormData(g=>{
      g.dataourceColumnList = parma;
    })
    //数据发生变化后续重置依赖数据源的hash设置,附加结果字段
    resetHashConfig();
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
    if(!status ||(role === ROLE_TYPE.PROVIDER && status === JOB_STATUS.AUDITING) ){
      return false
    }
    return true
  }

  // 选择布隆过滤器回调
  const onBloomFilterSelectedCallBack = (hash_config:any) => {
    formRef.setFieldsValue({hash_config:{
      ...hash_config,
      source:'setFieldsValue'
    }})
  }

  // 数据源类型变化清空主键设置
  const onDataSourceTypeChange = (e:any)=>{
    resetHashConfig()
  }

  const resetHashConfig = ()=>{
    formRef.setFieldsValue({hash_config:{
      list:[],
      source:'setFieldsValue'
    },
    additional_result_columns:[]})
  }
  
  const checkAlgorithmDisable = ()=>{
    let flag = checkFormDisable();
    const role = lodash.get(detailData,'jobDetailData.role','')
    return flag || role === ROLE_TYPE.PROVIDER
  }

  const validateSelfField =  (_: any, value:any)  =>{
    const result = IsEmptyObject(value)
    if(result){
      return Promise.reject(new Error('此项不能为空'))
    } else {
      return Promise.resolve()
    }
  }


  return <>
      <Spin spinning={loading} >
      <Row justify="center" className="form-scroll">
          <Col lg={{span: 16}} md={{span: 24}}>
            <Form
              form={formRef}
              initialValues={{data_resource_type:'TableDataSource',add_method:'HttpUpload',algorithm:'rsa_psi'}}
              layout="vertical"
              disabled={checkFormDisable()}
            >
              <Form.Item name="algorithm" label="算法类型" required>
                <Select style={{width:200}} disabled={checkAlgorithmDisable() }>
                  {jobFormData.algorithmList.map((item:string) => (
                    <Select.Option key={item} value={item}>
                      {item}
                    </Select.Option>
                    ))}
                </Select>
              </Form.Item>
              <Form.Item style={{marginBottom:0}}  label="样本类型" required>
                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.algorithm !== cur.algorithm }>
                  {({ getFieldValue }) => {
                      const algorithm = getFieldValue('algorithm'); 
                      return  <Form.Item name="data_resource_type" style={{ display: 'inline-block', marginBottom: 0 }} rules={[{ required: true }]}>
                      <Radio.Group onChange={onDataSourceTypeChange}>
                        {[...dataResourceTypeMap].map(([value, label]) => {
                          if(algorithm === 'ecdh_psi' && value === 'PsiBloomFilter'){
                            return null
                          }
                          return  <Radio key={value} value={value} >
                          {label}
                        </Radio>
                        })}
                      </Radio.Group>
                    </Form.Item>
                  }}
                </Form.Item>
                
                <Form.Item noStyle shouldUpdate={(prev, cur) => prev.data_resource_type !== cur.data_resource_type|| prev.add_method !== cur.add_method  }>
                  {({ getFieldValue }) => {
                      const data_resource_type = getFieldValue('data_resource_type');
                      const add_method = getFieldValue('add_method');
                      // 选择数据集
                      if(data_resource_type === DATARESOURCE_TYPE.TABLE_DATASOURCE) {
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
                         <Form.Item name={'table_data_resource_info'}   rules={[{ validator: validateSelfField }]}>
                         {add_method === 'HttpUpload'?<FileChunkUpload uploadFinishCallBack={prevColumnsChangeCallBack}  disabled={checkFormDisable()}/>:
                            <DataSourceForm prevColumnsChangeCallBack={prevColumnsChangeCallBack} disabled={checkFormDisable()}/>
                          }
                         </Form.Item>
                        </>
                      } else {
                       return  <Form.Item style={{marginTop:30}} name="bloom_filter_resource_input" label="选择布隆过滤器" rules={[formRuleRequire('此项不能为空')]}>
                            <BloomFilterFormItem onBloomFilterSelectedCallBack={onBloomFilterSelectedCallBack}/>
                        </Form.Item>
                      }
                    }
                  }
                </Form.Item>
              </Form.Item>
              <Form.Item noStyle shouldUpdate={(prev, cur) => prev.data_resource_type !== cur.data_resource_type }>
                {({ getFieldValue }) => {
                  const data_resource_type = getFieldValue('data_resource_type');
                  const hashFormDisabled = data_resource_type === DATARESOURCE_TYPE.PSI_BLOOM_FILTER?true:false
                  const resultArray = [<Form.Item name={'hash_config'} style={{marginBottom:0}} rules={[{ validator: validateSelfField }]}>
                        <HashForm disabled={checkFormDisable()||hashFormDisabled} columnList={jobFormData.dataourceColumnList}/> 
                      </Form.Item>];
                  if(data_resource_type != DATARESOURCE_TYPE.PSI_BLOOM_FILTER){
                    resultArray.push(<Form.Item name={'additional_result_columns'} label="附加结果字段" style={{marginTop:20}}>
                       <Select mode="multiple" style={{ width: 300 }} placeholder="请选择字段">
                        {jobFormData.dataourceColumnList.map((item:string) => (
                          <Select.Option key={item} value={item}>
                            {item}
                          </Select.Option>
                          ))}
                       </Select>
                    </Form.Item>)
                  }
                  return <>
                  {resultArray}
                  </>

                }}
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
    </>
});

export default JobForm;
