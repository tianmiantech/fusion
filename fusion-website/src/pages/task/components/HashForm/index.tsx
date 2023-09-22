import React, { useEffect, useState, useRef } from 'react';
import { Form, Select, Space } from 'antd';
import { MinusCircleTwoTone, PlusCircleTwoTone } from '@ant-design/icons';
import { encryMethodMap } from '@/constant/dictionary';
import './index.less';

const HashForm = () => {
  const features = [{
    label: 'id',
    value: 'id'
  }, {
    label: 'x1',
    value: 'x1'
  }]

  const HashKeyForm = () => (
    <>
      <Form.List name="hashValues">
        {(fields, { add, remove }) => {
          return(
          <>
            {fields.map(({key, name}, index) => (
              <Space key={key} style={{ width: '100%' }}>
                <Form.Item name={[name, 'columns']} className="hash-form-item">
                  <Select mode="multiple" style={{ width: 200 }} placeholder="请选择字段">
                    {features.map(feature => (
                      <Select.Option key={feature.value} value={feature.value}>
                        {feature.label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
                <Form.Item name={[name, 'method']} className="hash-form-item">
                  <Select style={{ width: 100 }} placeholder="加密方式">
                    {[...encryMethodMap].map(([value, label]) => (
                      <Select.Option key={value} value={value}>
                        {label}
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
          </>
        )}}
      </Form.List>
    </>
  )

  return (
    <>
      <HashKeyForm />
    </>
  );
};

export default HashForm;
