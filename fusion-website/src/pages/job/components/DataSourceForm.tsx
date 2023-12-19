import React, { useEffect, useState } from 'react';
import { Form, Select, Input, Button, Space, Row, Col,FormInstance,Spin,message } from 'antd';
import { FolderOpenOutlined } from '@ant-design/icons';
import { useMount,useRequest } from 'ahooks';
import { useModel } from '@umijs/max';
import lodash from 'lodash'
import SmUtil from '@/utils/SmUtil'
import { testDataSource,TestDataSourceInterface  } from '../service';
import {DataPreviewBtn} from '@/components/DataSetPreview'
import type {PeviewDataRequestInterface} from '@/components/DataSetPreview/service'

interface DataSourceFormInterface {
  disabled?:boolean,
  value?:any,
  onChange?: (value:any)=>void,
  prevColumnsChangeCallBack?: (columns:any[])=>void
}

const DataSourceForm = (props:DataSourceFormInterface) => {
  const {disabled=false,value,onChange,prevColumnsChangeCallBack} = props
  const {dataSoureConfig,
    checkIfNeedGetDataSourceAvailableType,
  } = useModel('job.useDataSourceForm')

  const [dataSourceFormRef] = Form.useForm()

  const {getEncryptPublicKey} = useModel('initializeConfig')

  //展示测试成功的数据源预览
  const [successCheck, setSuccessCheck] = useState<boolean>(false);

  const [preViewDataRequest,setPreViewDataRequest] = useState<PeviewDataRequestInterface>({
    add_method:'Database',
  })

  useEffect(()=>{
    if(value){
      //表示Form表单中主动设置值
      const source = lodash.get(value,'source','')
      if(source === 'setFieldsValue'){
        const data_source_params = lodash.get(value,'data_source_params',{})
        dataSourceFormRef.setFieldsValue(data_source_params);
        setPreViewDataRequest(value)
        setSuccessCheck(true)
      } 
    }
  },[value])
  

  

   //测试数据源是否可以用
   const {run:runTestDataSource,loading:testDataSourceLoading} = useRequest(async (params:TestDataSourceInterface)=>{
    const reponse = await testDataSource(params)
    const {code,data} = reponse
    const msg = lodash.get(data,'message')
    const success = lodash.get(data,'success')
    if(code === 0 && success){
      setSuccessCheck(true)
      message.success(msg)
    }else{
      setSuccessCheck(false)
      message.error(msg)
    }
  },{
      manual:true,
  })



  useMount(()=>{
    checkIfNeedGetDataSourceAvailableType()
  })

  const testConnection =async ()=>{
    const formResult = await dataSourceFormRef.validateFields();
    const publicKey = await getEncryptPublicKey()
    const requestParams= {
      database_type:formResult.database_type,
      data_source_params:{
        ...formResult
      }
    }
    if(formResult.password){
      requestParams.data_source_params.password = SmUtil.encryptByPublicKey(publicKey,formResult.password)
    }
    runTestDataSource(requestParams)
    const prewViewDataRequest = {
      add_method:'Database',
      sql:formResult.sql,
      ...requestParams
    } as PeviewDataRequestInterface
    setPreViewDataRequest(prewViewDataRequest)
    onChange && onChange({...prewViewDataRequest})

  }

  const onDataSourceFormChange = (changedValues:any, allValues:any)=>{
   //表单变动时，需要重新测试数据源可用性
   if(successCheck)
      setSuccessCheck(false);
  }

  
  const formItemLayout = {
    style: {
      marginBottom: 12
    },
    rules:[{
      required: true,
      message: '此项不能为空',
    }]
  }

  const columnsChangeCallBack = (columns:any[])=>{
    prevColumnsChangeCallBack && prevColumnsChangeCallBack(columns)
  }
  

 

  const BaseDataSourceForm = () => (
    <>
      <Form.Item name="databaseName" label="默认数据库名称" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      <Form.Item name="username" label="用户名" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      <Form.Item name="password" label="密码" {...formItemLayout}>
        <Input.Password placeholder='请输入' autoComplete="one-time-code" />
      </Form.Item>
    </>
  );

  const HiveForm = () => (
    <>
      <Form.Item name="metastorePort" label="Metastore端口" {...formItemLayout} initialValue={9083}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      <Form.Item name="databaseName" label="默认数据库名称" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
    </>
  )
    
  return (
    <Spin spinning={testDataSourceLoading}>
      <Form form={dataSourceFormRef} onValuesChange={onDataSourceFormChange} disabled={disabled}>
        <Row
          justify={'center'}
          style={{ width: '95%', backgroundColor: '#fbfbfb', padding: '10px 10px 10px ' }}
        >
          
          <Col span={24}>
          <Form.Item name="database_type" label="数据源类型" {...formItemLayout}>
            <Select options={dataSoureConfig.dataSoureTypeList}>
            </Select>
          </Form.Item>
          <Form.Item name="host" label="Host" {...formItemLayout}>
            <Input placeholder='请输入'></Input>
          </Form.Item>
          <Form.Item name="port" label="JDBC端口" {...formItemLayout}>
            <Input placeholder='请输入'></Input>
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, cur) => prev.database_type !== cur.database_type }>
            {({ getFieldValue }) => {
                const dbType = getFieldValue('database_type');
                if (dbType) {
                  if (dbType === 'Hive') return <HiveForm />;
                  else return <BaseDataSourceForm />
                }
              }
            }
          </Form.Item>
          <Form.Item name="sql" label="查询语句" {...formItemLayout} initialValue={"select * from account"}>
            <Input.TextArea
              rows={6}
              placeholder={`select * from table where id < 30000000;\nselect * from table1 where id >= 30000000 and id < 60000000\nunion\nselect * from table2 where score >= 6 and score < 10;`}
            ></Input.TextArea>
          </Form.Item>
          <Form.Item {...formItemLayout}>
            <Space>
              <Button
                className="success-plain-btn"
                onClick={() => {testConnection()}}
              >查询测试</Button>
              {
                successCheck ? <DataPreviewBtn autoLoadPreView={true} requestParams={preViewDataRequest} columnsChangeCallBack={columnsChangeCallBack}/>  : null
              }
            </Space>
          </Form.Item>
          </Col>
        
        </Row>
    </Form>
    </Spin>
  );
};

export default DataSourceForm;
