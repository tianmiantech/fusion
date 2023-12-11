import React, { useEffect, useState } from 'react';
import { Form, Select, Input, Button, Space, Row, Col,FormInstance } from 'antd';
import { FolderOpenOutlined } from '@ant-design/icons';
import { useMount } from 'ahooks';
import { useModel } from '@umijs/max';
import lodash from 'lodash'
interface DataSourceFormInterface {
  formRef:FormInstance
}
const DataSourceForm = (props:DataSourceFormInterface) => {
  const {formRef} = props
  const {dataSoureConfig,
    checkIfNeedGetDataSourceAvailableType,
    runTestDataSource,
    testDataSourceCallBakData
  } = useModel('job.useDataSourceForm')
  //展示测试成功的数据源预览
  const [successCheck, setSuccessCheck] = useState<boolean>(false);


  useMount(()=>{
    checkIfNeedGetDataSourceAvailableType()
  })

  const testConnection = ()=>{
    let defaultFields = ['databaseType','host','port','sql']
    const databaseType = formRef.getFieldValue('databaseType')||'';
    if(databaseType){
      if(lodash.toLower(databaseType) === 'mysql' || lodash.toLower(databaseType) === 'doris') {
        defaultFields = defaultFields.concat(['databaseName','userName','password'])
      } else if(lodash.toLower(databaseType) === 'hive'){
        defaultFields = defaultFields.concat(['databaseName','metastorePort'])
      }
    }
    formRef.validateFields(defaultFields).then(res=>{
      runTestDataSource(res)
      
    }).catch(e=>{
      console.log(e);
      
    })
  }

  useEffect(()=>{
    const code  = lodash.get(testDataSourceCallBakData,'code')
    if(code == 0){
      setSuccessCheck(true)
    }
  },[testDataSourceCallBakData])
  
  const formItemLayout = {
    style: {
      marginBottom: 12
    },
    rules:[{
      required: true,
      message: '此项不能为空',
    }]
  }
  

 

  const BaseDataSourceForm = () => (
    <>
      <Form.Item name="databaseName" label="默认数据库名称" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      <Form.Item name="userName" label="用户名" {...formItemLayout}>
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
    <Row
      justify={'center'}
      style={{ width: '95%', backgroundColor: '#fbfbfb', padding: '10px 10px 10px ' }}
    >
      <Col span={24}>
      <Form.Item name="databaseType" label="数据源类型" {...formItemLayout}>
        <Select options={dataSoureConfig.dataSoureTypeList}>
        </Select>
      </Form.Item>
      <Form.Item name="host" label="Host" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      <Form.Item name="port" label="JDBC端口" {...formItemLayout}>
        <Input placeholder='请输入'></Input>
      </Form.Item>
      <Form.Item noStyle shouldUpdate={(prev, cur) => prev.databaseType !== cur.databaseType }>
        {({ getFieldValue }) => {
            const dbType = getFieldValue('databaseType');
            if (dbType) {
              if (dbType === 'Hive') return <HiveForm />;
              else return <BaseDataSourceForm />
            }
          }
        }
      </Form.Item>
      <Form.Item name="sql" label="查询语句" {...formItemLayout}>
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
            successCheck ? <Button icon={<FolderOpenOutlined />}>预览</Button> : null
          }
        </Space>
      </Form.Item>
      </Col>
    </Row>
  );
};

export default DataSourceForm;
