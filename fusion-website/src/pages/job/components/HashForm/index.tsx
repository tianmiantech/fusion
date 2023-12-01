import React, { useEffect, useState, useRef } from 'react';
import { MinusCircleTwoTone, PlusCircleTwoTone } from '@ant-design/icons';
import { Input, Button, Form, Select, Upload, Tooltip, Space, Row, Col,Alert,Spin } from 'antd';
import { FolderOpenOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { encryMethodMap } from '@/constant/dictionary';
import './index.less';
import lodash from 'lodash'

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
}
const HashForm = (props:HashFormPropsInterface) => {
  const {columnList,value,onChange} = props

  const onValuesChange = (changedValues:any, allValues:any) => {
    const valueList = lodash.get(allValues,'valueList',[])
    onChange?.({list:valueList})
  };

  const HashKeyForm = () => (
    <>
      <Form.List name="valueList" >
        {(fields, { add, remove,...rest }) => {
          console.log("rest",rest);
          console.log("fields",fields);
          
          return(
          <>
            {fields.map(({key, name}, index) => (
              <Space key={key} style={{ width: '100%' }}>
                 <Form.Item name={[name, 'method']} className="hash-form-item">
                  <Select style={{ width: 100 }} placeholder="加密方式">
                    {[...encryMethodMap].map(([value, label]) => (
                      <Select.Option key={value} value={value}>
                        {label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
                <Form.Item name={[name, 'columns']} className="hash-form-item">
                  <Select mode="multiple" style={{ width: 200 }} placeholder="请选择字段">
                    {columnList.map(feature => (
                      <Select.Option key={feature} value={feature}>
                        {feature}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>

                <div className="operation-group">
                  <MinusCircleTwoTone
                    twoToneColor="#ff7875"
                    className="operation-btn minus-btn"
                    onClick={() => remove(index)}
                  />
                  { index === fields.length - 1 ?
                    <PlusCircleTwoTone className="operation-btn" onClick={() => add()} /> : null }
                </div>
              </Space>
            ))}
            {
              fields.length ==0  &&  <Button onClick={()=>{add()}}>设置主键</Button>
            }
          </>
        )}}
      </Form.List>
    </>
  )

  return (
    <Form onValuesChange={onValuesChange}>
      <Form.Item
        label={
          <>
            <Tooltip
              placement="top"
              title={<span>* 设置的融合主键是标明样本的对齐字段；<br/>
              * 设置的融合主键不宜过长，主键的hash处理后的长度越长对齐耗时越多；<br/>
              * 如需多个样本标识，建议字段拼接后用一种hash方式处理(例：MD5(account+cnid))；<br/>
              * 设置的融合主键需要和合作方的过滤器的融合主键处理方式一致。</span>}
              overlayStyle={{ maxWidth: 350 }}
            >
              设置融合主键hash方式&nbsp;<QuestionCircleOutlined />
            </Tooltip>
          </>
        }
        required
      >
        <HashKeyForm />
      </Form.Item>
    </Form>
  );
};

export default HashForm;
