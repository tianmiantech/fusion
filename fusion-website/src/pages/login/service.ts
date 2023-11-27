import {request}from '@/utils/request'

export interface UserRequestParams {
  username: string,
  password: string,
}


//初始化用户
export const initUser=(params:UserRequestParams)=>{
    return request.post("/service/init",params)
}

//初始化用户
export const login =(params:UserRequestParams)=>{
  return request.post("/account/login",params)
}