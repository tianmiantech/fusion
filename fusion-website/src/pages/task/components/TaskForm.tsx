import React, { useEffect, useState, useRef, forwardRef, useImperativeHandle } from 'react';
import { Input, Button, Form, Radio, Upload, Tooltip, Space, Row, Col,Alert,Spin } from 'antd';
import type { UploadChangeParam } from 'antd/es/upload';
import type { RcFile, UploadProps, UploadFile } from 'antd/es/upload/interface';
import { FolderOpenOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { dataResourceTypeMap, dataSetAddMethodMap } from '@/constant/dictionary';
import DataSetPreview from "./DataSetPreview";
import HashForm from './HashForm/index';
import DataSourceForm from './DataSourceForm';
import BloomFilterManage from './BloomFilterManage';
import { formRuleRequire } from '@/utils/common';
import { getBaseURL,getToken } from '@/utils/request'
import { useImmer } from 'use-immer';
import lodash from 'lodash'
import FileChunkUpload from '@/components/FileChunkUpload'
interface DataInterface {
  previewOpen:boolean,
  BFManageOpen:boolean,
  file:UploadFile<any>|null, //上传的文件的对象
  fileUploadLoading:boolean, //文件上传loading状态
  fileMessage:string, //文件上传提示
  fileMessageType:"info" | "success" | "warning" | "error" //文件上传提示类型
}

const TaskForm = forwardRef((props, ref) => {
  const [formRef] = Form.useForm();

  const [fields, setFields] = useState([
    { name: ['dataResourceType'], value: 'DataSet' },
    { name: ['dataSetAddMethod'], value: 'file' },
    { name: ['hashValues'], value: [] }
  ]);

  const [datas,setDatas] = useImmer<DataInterface>({
    previewOpen:false,
    BFManageOpen:false,
    file:null,
    fileUploadLoading:false,
    fileMessage:'支持.csv .xls .xlsx 文件类型',
    fileMessageType:'info'
  })
  
  const uploadProps:UploadProps = {
    name: 'file',
    maxCount: 1,
    accept:".csv,application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    action:`${getBaseURL()}/file/upload`,
    headers:{
      'X-User-Token':getToken()
    },
    data(file){
      return {
        upload_file_use_type:'PsiBloomFilter',
        filename:file.name
      }
    },
    onChange(info:UploadChangeParam<UploadFile>) {
      onFileUploadChange(info)
    }
  };

  const onFileUploadChange = (info:UploadChangeParam<UploadFile>)=>{
    const status = lodash.get(info,'file.status')
    if (status === 'uploading') {
      setDatas(g=>{
        g.fileUploadLoading = true
      })
    } else if (status === 'done') {
      const response = lodash.get(info,'file.response')
      const {code,message} = response
      if(code !== 0){
        const test = formRef.getFieldValue('uploadFile')
        setDatas(g=>{
          g.fileMessageType = 'error'
          g.fileMessage = message||'上传出错,请稍后重试'
          g.file = null
        })
      } else {
        setDatas(g=>{
          g.file = info.file
        })
      }
      setDatas(g=>{
        g.fileUploadLoading = false
      })
    } else if (status === 'error'){
      setDatas(g=>{
        g.fileUploadLoading = false
        g.fileMessageType = 'error'
        g.fileMessage= '文件上传失败，请检查后再继续'
      })
    }
  } 
  
  const setHashKey = () => {
    formRef.setFieldsValue({
      hashValues: [{
        columns: [],
        method: null
      }]
    })
  }

  const validForm = () => {
    formRef.validateFields();
  }

  useImperativeHandle(ref, () => ({
    validForm
  }));

  return (
    <Spin spinning={datas.fileUploadLoading}>
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
                    <Button
                      style={{ marginLeft: 15 }}
                      onClick={()=>{}}
                    >布隆过滤器管理</Button> : null
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
                        <>
                        {/* <Alert style={{marginBottom:5}} type={datas.fileMessageType} message={datas.fileMessage}/>
                        <Space align="start">
                          <Upload {...uploadProps}>
                            <Button icon={<FolderOpenOutlined />}>选择文件</Button>
                          </Upload>
                          {
                            datas.file?.name ?
                            <Button
                              icon={<FolderOpenOutlined />}
                              onClick={() => {}}
                            >预览</Button> : ''
                          }
                        </Space> */}
                        <FileChunkUpload/>
                        </> :
                        <DataSourceForm formRef={formRef}/>
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
      {/* 数据预览 */}
      <DataSetPreview
        open={datas.previewOpen}
        onCancel={() => {setDatas(g=>{g.previewOpen = false})}}
      />
      {/* 布隆过滤器管理 */}
      <BloomFilterManage
        open={datas.BFManageOpen}
        onClose={() => {setDatas(g=>{g.BFManageOpen = false})}}
      />
    </Spin>
  );
});

export default TaskForm;
