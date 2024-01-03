import {request}from '@/utils/request'

export interface UserRequestParams {
  username: string,
  password: string,
}


//初始化用户
export const initUser=(params:UserRequestParams)=>{
    return request.post("/service/init",params)
}

//登录
export const login =(params:UserRequestParams)=>{
  return request.post("/account/login",params)
}

//添加用户
export const addUser =(params:UserRequestParams)=>{
  return request.post("/account/add",params)
}