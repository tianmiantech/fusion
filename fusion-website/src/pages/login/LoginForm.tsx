import { Button, Checkbox, Form, Input, Card, message } from 'antd';
import React from 'react';
import { initUser,login,UserRequestParams,addUser } from './service'
import { useRequest } from 'ahooks';
import { history } from '@umijs/max';
import {getTokenName} from '@/utils/utils'
import {setCookies} from '@tianmiantech/util'
import lodash from 'lodash'
import SmUtil from '@/utils/SmUtil';
import {FUNSION_INITIALIZED_KEY} from '@/constant/dictionary'



const LOGIN_FORM_TYPE = {
  LOGIN: 'login',
  REGISTER: 'register',
  ADDUSER: 'addUser',
};

interface LoginFormProps {
  formType:string //是否是系统初始化，如果是初始化则需要确认密码
}


const Index = (props: LoginFormProps) => {
  const {formType=LOGIN_FORM_TYPE.LOGIN} = props

  const {run:initUserHandler,loading:initUserLoading} = useRequest(async (params)=>{
    const reponse = await initUser(params)
    const {code} = reponse
    if(code === 0 ){
      message.info('初始化成功')
      localStorage.setItem(FUNSION_INITIALIZED_KEY,'true')
      const token = lodash.get(reponse,'data.token')
      setCookies({[getTokenName()]:token})
      setTimeout(()=>{
        history.replace('/home')
      },800)
    }
  },{manual:true})

  const {run:loginHandler,loading:loginLoading} = useRequest(async (params)=>{
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
  },{manual:true})

  const {run:addUserHandler,loading:addUserLoading} = useRequest(async (params)=>{
    const reponse = await addUser(params)
    const {code} = reponse
    if(code === 0 ){
      message.info('添加成功')
      setTimeout(()=>{
        history.replace('/home')
      },800)
    }
  },{manual:true})

  const getLoading=() =>{
    return loginLoading || initUserLoading || addUserLoading
  }
  

  const onFinish = (values: UserRequestParams) => {
    const {username,password} = values
    const requestParams = {username,password:SmUtil.encrypt(`_${username}-${password}_${username}@!#`)}
    if(formType === LOGIN_FORM_TYPE.LOGIN){
      loginHandler(requestParams)
    } else if(formType === LOGIN_FORM_TYPE.REGISTER){
      initUserHandler(requestParams)
    } else if(formType === LOGIN_FORM_TYPE.ADDUSER){
      addUserHandler(requestParams)
    }
  };

  const getBtntitle = ()=>{
    if(formType === LOGIN_FORM_TYPE.LOGIN){
      return '登录'
    } else if(formType === LOGIN_FORM_TYPE.REGISTER){
      return '初始化'
    } else if(formType === LOGIN_FORM_TYPE.ADDUSER){
      return '添加用户'
    }
  }

  const getLoginFormTitle = ()=>{
    if(formType === LOGIN_FORM_TYPE.LOGIN){
      return '用户登录'
    } else if(formType === LOGIN_FORM_TYPE.REGISTER){
      return '系统初始化'
    } else if(formType === LOGIN_FORM_TYPE.ADDUSER){
      return '添加用户'
    }
  }



  return <Card title={getLoginFormTitle()} style={{width:500}} >
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
      {(formType===LOGIN_FORM_TYPE.ADDUSER||formType === LOGIN_FORM_TYPE.REGISTER) &&  <Form.Item
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
        <Button type="primary" htmlType="submit" loading={getLoading()}>
          {getBtntitle()}
        </Button>
      </Form.Item>
    </Form>
  </Card>
};
Index.LOGIN_FORM_TYPE = LOGIN_FORM_TYPE
export default Index