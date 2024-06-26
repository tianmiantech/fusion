import React, { useEffect, useState, useRef } from 'react';
import { MinusCircleTwoTone, PlusCircleTwoTone } from '@ant-design/icons';
import { Input, Button, Form, Select, Upload, Tooltip, Space, Row, Col,Alert,Spin } from 'antd';
import { FolderOpenOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { encryMethodMap } from '@/constant/dictionary';
import './index.less';
import lodash from 'lodash'
import { useMount } from 'ahooks';

export interface HashFormValue {
  list:{
    columns:string[],
    method:string
  }[]
}
interface HashFormPropsInterface  {
  columnList:string[]
  value?:HashFormValue
  onChange?:(value:HashFormValue)=>void
  disabled?:boolean
}
const HashForm = (props:HashFormPropsInterface) => {
  const {columnList,value,onChange,disabled} = props
  const [formRef] = Form.useForm();  
  
  //编辑时，Form主动设置value，将数据进行回填
  useEffect(()=>{
    if(value){
      const source = lodash.get(value,'source','')
      if(source === "setFieldsValue") {
        const valueList = lodash.get(value,'list',[{}])
        if(valueList.length == 0) {
          valueList.push({})
        }
        formRef.setFieldValue('valueList',valueList)
      }
    } else {
      formRef.setFieldsValue({valueList:[{}]})
    }
  },[value])


  const onValuesChange = (changedValues:any, allValues:any) => {
    const valueList = lodash.get(allValues,'valueList',[])
    onChange?.({list:valueList})
  };

  const HashKeyForm = () => (
    <>
      <Form.List name="valueList" >
        {(fields, { add, remove,...rest }) => {
          return(
          <>
            {fields.map(({key, name}, index) => (
              <Space key={key} style={{ width: '100%' }}>
                 <Form.Item name={[name, 'method']} className="hash-form-item">
                  <Select disabled={disabled} style={{ width: 100 }} placeholder="脱敏方式">
                    {[...encryMethodMap].map(([value, label]) => (
                      <Select.Option key={value} value={value}>
                        {label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
                <Form.Item name={[name, 'columns']} className="hash-form-item">
                  <Select disabled={disabled}   mode="multiple" style={{ width: 300 }} placeholder="请选择字段">
                    {columnList.map(feature => (
                      <Select.Option key={feature} value={feature}>
                        {feature}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>

                <div className="operation-group">
                 {
                     (index>0  && !disabled) ? <MinusCircleTwoTone
                    twoToneColor="#ff7875"
                    className="operation-btn minus-btn"
                    onClick={() => remove(index)}
                    key={index}
                  />:null
                 }
                  { (index === fields.length - 1  && !disabled) ?
                    <PlusCircleTwoTone    key={index} className="operation-btn" onClick={() => add()} /> : null }
                </div>
              </Space>
            ))}
            {/* {
              fields.length ==0 &&  <Button disabled={disabled} onClick={()=>{add()}}>设置主键</Button>
            } */}
          </>
        )}}
      </Form.List>
    </>
  )

  return (
    <Form onValuesChange={onValuesChange} form={formRef}>
      <Form.Item
        label={'设置融合主键hash方式'}
        required
        style={{ marginBottom: 0 }}
      >
        <HashKeyForm />
      </Form.Item>
    </Form>
  );
};

export default HashForm;
