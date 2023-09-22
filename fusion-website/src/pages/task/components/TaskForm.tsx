import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col } from 'antd';
import type { UploadChangeParam } from 'antd/es/upload';
import type { RcFile, UploadProps, UploadFile } from 'antd/es/upload/interface';
import { FolderOpenOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { dataResourceTypeMap, dataSetAddMethodMap } from '@/constant/dictionary';
import DataSetPreview from "./DataSetPreview";
import HashForm from './HashForm/index';
import DataSourceForm from './DataSourceForm';
import { formRuleRequire } from '@/utils/common';

interface FormData {
  dataResourceType: string;
}

const TaskForm = forwardRef((props, ref) => {
  const [formRef] = Form.useForm();
  const [fields, setFields] = useState([
    { name: ['dataResourceType'], value: 'DataSet' },
    { name: ['dataSetAddMethod'], value: 'file' },
    { name: ['hashValues'], value: [] }
  ]);
  const [file, setFile] = useState<UploadFile>();
  const [previewOpen, setPreviewOpen] = useState(false);

  /* useImperativeHandle(ref, () => ({
    getIsReady: () => {
      return isReady
    }
  })); */

  const uploadProps:UploadProps = {
    name: 'file',
    maxCount: 1,
    onChange(info:UploadChangeParam<UploadFile>) {
      console.log('info', info);
      if (info.file.status === 'done') {
        setFile(info.file);
      }
    }
  };

  const showPreview = () => {
    setPreviewOpen(true);
  }

  const setHashKey = () => {
    formRef.setFieldsValue({
      hashValues: [{
        columns: [],
        method: null
      }]
    })
  }

  return (
    <>
      <Row justify="center" className="form-scroll">
        <Col lg={{span: 16}} md={{span: 24}}>
          <Form
            form={formRef}
            fields={fields}
            layout="vertical"
          >
            <Form.Item label="样本类型" required>
              <Form.Item name="dataResourceType" style={{ display: 'inline-block', marginBottom: 0 }} rules={[{ required: true }]}>
                <Radio.Group>
                  {[...dataResourceTypeMap].map(([value, label]) => (
                    <Radio.Button key={value} value={value}>
                      {label}
                    </Radio.Button>
                  ))}
                </Radio.Group>
              </Form.Item>
              <Form.Item noStyle shouldUpdate={(prev, cur) => prev.dataResourceType !== cur.dataResourceType }>
                {({ getFieldValue }) => {
                    return getFieldValue('dataResourceType') === 'BloomFilter' ?
                    <Button style={{ marginLeft: 15 }}>布隆过滤器管理</Button> : null
                  }
                }
              </Form.Item>
            </Form.Item>
            <Form.Item noStyle shouldUpdate={(prev, cur) => prev.dataResourceType !== cur.dataResourceType }>
              {({ getFieldValue }) => 
                getFieldValue('dataResourceType') === 'DataSet' ?
                <>
                  <Form.Item name="dataSetAddMethod" label="选择样本" rules={[formRuleRequire()]}>
                    <Radio.Group>
                      {[...dataSetAddMethodMap].map(([value, label]) => (
                        <Radio key={value} value={value}>
                          {label}
                        </Radio>
                      ))}
                    </Radio.Group>
                  </Form.Item>
                  <Form.Item>
                    <Form.Item noStyle shouldUpdate={(prev, cur) => prev.dataSetAddMethod !== cur.dataSetAddMethod }>
                      {({ getFieldValue }) => 
                        getFieldValue('dataSetAddMethod') === 'file' ?
                        <Space align="start">
                          <Upload {...uploadProps}>
                            <Button icon={<FolderOpenOutlined />}>选择文件</Button>
                          </Upload>
                          {
                            file?.name ? <Button icon={<FolderOpenOutlined />} onClick={showPreview}>预览</Button> : ''
                          }
                        </Space> :
                        <DataSourceForm />
                      }
                    </Form.Item>
                  </Form.Item>
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
                  >
                    <Form.Item noStyle shouldUpdate={(prev, cur) => prev.hashValues?.length !== cur.hashValues?.length }>
                      {
                        ({ getFieldValue }) => {
                          return getFieldValue('hashValues')?.length > 0 ?
                            <HashForm /> :
                            <Button onClick={setHashKey}>设置主键</Button>
                        }
                      }
                    </Form.Item>
                  </Form.Item>
                </> :
                <>
                  <Form.Item label="选择样本" required>
                    <Space align="start">
                      <Upload {...uploadProps}>
                        <Button icon={<FolderOpenOutlined />}>选择文件</Button>
                      </Upload>
                    </Space>
                  </Form.Item>
                  <Form.Item label="主键">MD5(id+y)+SHA256(x0)</Form.Item>
                </>
              }
            </Form.Item>
            <Form.Item name="remark" label="任务备注">
              <Input.TextArea rows={4} placeholder="请输入" />
            </Form.Item>
          </Form>
        </Col>
      </Row>
      <DataSetPreview
        open={previewOpen}
        onCancel={() => setPreviewOpen(false)}
      />
    </>
  );
});

export default TaskForm;
