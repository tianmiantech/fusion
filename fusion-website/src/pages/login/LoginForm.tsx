import { Button, Checkbox, Form, Input, Card, message } from 'antd';
import React from 'react';
import { initUser,login,UserRequestParams } from './service'
import { useRequest } from 'ahooks';
import { history } from '@umijs/max';
import {getTokenName} from '@/utils/index'
import {setCookies} from '@tianmiantech/util'
import lodash from 'lodash'
import Sm3Util from '@/utils/Sm3Util';
import {FUNSION_INITIALIZED_KEY} from '@/constant/dictionary'
interface LoginFormProps {
  isRegister?:boolean //是否是系统初始化，如果是初始化则需要确认密码
}


const Index = (props: LoginFormProps) => {
  const {isRegister=false} = props
  const registerHandler = async(params:UserRequestParams): Promise<null> =>{
    const reponse = await initUser(params)
    const {code} = reponse
    if(code === 0 ){
      message.info('初始化成功')
      localStorage.setItem(FUNSION_INITIALIZED_KEY,'true')
      setTimeout(()=>{
        history.replace('/home')
      },800)
    }
    return null
  }
  const loginHandeler =  async(params:UserRequestParams): Promise<null> =>{
    const reponse = await login(params)
    const {code} = reponse
    if(code === 0 ){
      const token = lodash.get(reponse,'data.token')
      if(token){
        setCookies({[getTokenName()]:token})
        setTimeout(()=>{
          history.replace('/home')
        },800)
      } else {
        message.error('获取token失败')
      }
    }
    return null
  }


  const {run:submitData,loading } = useRequest(isRegister?registerHandler:loginHandeler,{manual:true})

 

  

  const onFinish = (values: UserRequestParams) => {
    const {username,password} = values
    submitData({username,password:Sm3Util.encrypt(`_${username}-${password}_${username}@!#`)})
  };



  return <Card title={isRegister?'系统初始化':'用户登录'} style={{width:500}} >
    <Form
      name="basic"
      labelCol={{span: 4}}
      wrapperCol={{span: 20}}
      onFinish={onFinish}
    >
      <Form.Item
        label="用户名"
        name="username"
        rules={[
          {
            required: true,
            message: '请输入用户名!',
          },
        ]}
      >
        <Input />
      </Form.Item>

      <Form.Item
        label="密码"
        name="password"
        rules={[
          {
            required: true,
            message: '请输入密码!',
          },
        ]}
      >
        <Input.Password />
      </Form.Item>
      {isRegister &&  <Form.Item
        label="确认密码"
        name="confirm"
        dependencies={['password']}
        rules={[
          {
            required: true,
            message: '请再次输入密码!',
          },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('password') === value) {
                return Promise.resolve();
              }
              return Promise.reject(
                new Error('两次输入密码不一致!'),
              );
            },
          }),
        ]}
      >
        <Input.Password />
      </Form.Item>}

      <Form.Item
        wrapperCol={{
          offset: 8,
          span: 16,
        }}
      >
        <Button type="primary" htmlType="submit" loading={loading}>
          {isRegister?'初始化':'登录'}
        </Button>
      </Form.Item>
    </Form>
  </Card>
};
export default Index