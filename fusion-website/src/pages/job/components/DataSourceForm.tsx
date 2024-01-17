import React, { useEffect, useState } from 'react';
import { Form, Select, Input, Button, Space, Row, Col,AutoComplete,Spin,message } from 'antd';
import { FolderOpenOutlined } from '@ant-design/icons';
import { useMount,useRequest } from 'ahooks';
import { useModel } from '@umijs/max';
import lodash from 'lodash'
import SmUtil from '@/utils/SmUtil'
import { testDataSource,TestDataSourceInterface  } from '../service';
import {DataPreviewBtn} from '@/components/DataSetPreview'
import type {PeviewDataRequestInterface} from '@/components/DataSetPreview/service'
import type {DataSourceListItemInterface} from '../models/useDataSourceForm'
import { useImmer } from 'use-immer';
import useCheckInitializedStore from '@/hooks/useCheckInitializedStore';
interface DataSourceFormInterface {
  disabled?:boolean,
  value?:any,
  onChange?: (value:any)=>void,
  prevColumnsChangeCallBack?: (columns:string[])=>void
}

interface SourceDataInterface {
  successCheck:boolean,
  dataSoureSuggestionList:DataSourceListItemInterface[],
  preViewDataRequest:PeviewDataRequestInterface
}

const DataSourceForm = (props:DataSourceFormInterface) => {

  const {disabled=false,value,onChange,prevColumnsChangeCallBack} = props

  const {dataSoureConfig,
    checkIfNeedGetDataSourceAvailableType,
  } = useModel('job.useDataSourceForm')

  const [dataSourceFormRef] = Form.useForm()

  const {getEncryptPublicKey} = useCheckInitializedStore()

  const [sourceData,setSourceData] = useImmer<SourceDataInterface>({
    successCheck:false,  //展示测试成功的数据源预览
    dataSoureSuggestionList:[],
    preViewDataRequest:{ //数据源预览请求参数
      add_method:'Database',
    }
  })

  useEffect(()=>{
    if(value){
      //表示Form表单中主动设置值
      const source = lodash.get(value,'source','')
      if(source === 'setFieldsValue'){
        const data_source_params = lodash.get(value,'data_source_params',{})
        dataSourceFormRef.setFieldsValue(data_source_params);
        //主动设置值时，预览数据源不需要传password
        setSourceData(draft=>{
          draft.preViewDataRequest = {...value,data_source_params:{
            ...data_source_params,
            password:null
          }}
          draft.successCheck = true
        })
      } 
    }
  },[value])

  //初始化自动补全数据源
  useEffect(()=>{
    if(dataSoureConfig.dataSoureSuggestion.length>0){
      const newList = dataSoureConfig.dataSoureSuggestion.map((item:DataSourceListItemInterface)=>{
        const {host,id} = item
        return {
          ...item,
          label:host,
          value:id
        }
      })
      setSourceData(draft=>{
        draft.dataSoureSuggestionList = newList
      })
    }
  },[dataSoureConfig.dataSoureSuggestion.length])
  

  

   //测试数据源是否可以用
   const {run:runTestDataSource,loading:testDataSourceLoading} = useRequest(async (params:TestDataSourceInterface)=>{
    const reponse = await testDataSource(params)
    const {code,data} = reponse
    const msg = lodash.get(data,'message')
    const success = lodash.get(data,'success')
    if(code === 0 && success){
      setSourceData(draft=>{
        draft.successCheck = true
      })
      message.success(msg)
    }else{
      setSourceData(draft=>{
        draft.successCheck = false
      })
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
    setSourceData(draft=>{
      draft.preViewDataRequest = prewViewDataRequest
    })
    onChange && onChange({...prewViewDataRequest})

  }

  const onDataSourceFormChange = (changedValues:any, allValues:any)=>{
   //表单变动时，需要重新测试数据源可用性
   if(sourceData.successCheck) {
    setSourceData(draft=>{
      draft.successCheck = false
    })
   }
  }

  const handleSearch = (value: string) => {
    const newList = sourceData.dataSoureSuggestionList.filter(item => item.host.indexOf(value) > -1);
    setSourceData(draft=>{
      draft.dataSoureSuggestionList = newList
    })
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
  
  const onDataSourceTypeChange = (value:string)=>{
    const newList = sourceData.dataSoureSuggestionList.filter(item => item.database_type.indexOf(value) > -1);
    setSourceData(draft=>{
      draft.dataSoureSuggestionList = newList
    })
  }

  const onHostSelected = (value:string)=>{
    const selectItem = lodash.find(sourceData.dataSoureSuggestionList,{id:value},null)
    if(selectItem){
      dataSourceFormRef.setFieldsValue({
        ...selectItem
      })
    }
  }

 

  const BaseDataSourceForm = () => (
    <>
    <Col span={12}>
      <Form.Item name="database_name" label="默认数据库名称" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      </Col>
      <Col span={12}>
      <Form.Item name="user_name" label="用户名" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      </Col>
      <Col span={12}>
      <Form.Item name="password" label="密码" {...formItemLayout}>
        <Input.Password placeholder='请输入' visibilityToggle={disabled?false:true}/>
      </Form.Item>
      </Col>
    </>
  );

  const HiveForm = () => (
    <>
    <Col span={12}>
      <Form.Item name="metastore_port" label="Metastore端口" {...formItemLayout} initialValue={9083}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      </Col>
      <Col span={12}>
      <Form.Item name="database_name" label="默认数据库名称" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      </Col>
    </>
  )
    
  return (
    <Spin spinning={testDataSourceLoading}>
      <Form layout="horizontal" form={dataSourceFormRef} onValuesChange={onDataSourceFormChange} disabled={disabled}>
        <Row
          gutter={24}
          style={{ backgroundColor: '#fbfbfb', padding: '10px 10px 10px ' }}
        >
          <Col span={12}>
            <Form.Item name="database_type" label="数据源类型" {...formItemLayout}>
              <Select options={dataSoureConfig.dataSoureTypeList} onSelect={onDataSourceTypeChange}>
              </Select>
            </Form.Item>
          </Col>
          <Col span={12}>
          <Form.Item name="host" label="Host" {...formItemLayout}>
            <AutoComplete 
              placeholder='请输入'
              options={sourceData.dataSoureSuggestionList}
              onSearch={handleSearch}
              onSelect={onHostSelected}
            />
          </Form.Item>
          </Col>
          <Col span={12}>
          <Form.Item name="port" label="JDBC端口" {...formItemLayout}>
            <Input placeholder='请输入'></Input>
          </Form.Item>
          </Col>
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
          <Col span={24}>
          <Form.Item name="sql" label="查询语句" {...formItemLayout} initialValue={"select * from account"}>
            <Input.TextArea
              rows={6}
              placeholder={`select * from table where id < 30000000;\nselect * from table1 where id >= 30000000 and id < 60000000\nunion\nselect * from table2 where score >= 6 and score < 10;`}
            ></Input.TextArea>
          </Form.Item>
          </Col>
          <Form.Item {...formItemLayout}>
            <Space>
              <Button
                className="success-plain-btn"
                onClick={() => {testConnection()}}
              >查询测试</Button>
              {
                sourceData.successCheck ? <DataPreviewBtn autoLoadPreView={true} requestParams={sourceData.preViewDataRequest} columnsChangeCallBack={columnsChangeCallBack}/>  : null
              }
            </Space>
          </Form.Item>
        
        </Row>
    </Form>
    </Spin>
  );
};

export default DataSourceForm;
