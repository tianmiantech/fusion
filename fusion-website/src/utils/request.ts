import { message } from 'antd';
import { axiosInstance, iam } from '@tianmiantech/request';
import utils from '@tianmiantech/util';
import { getTokenName } from './index';

const { getToken:getTokenByName, createUUID, formatDate } = utils;
const HOST_ENV = process.env.HOST_ENV;

const { generateOnTokenInvalid, invalidTokenCodes } = iam;


export const isQianKun = () => {
  return window.__POWERED_BY_QIANKUN__ || false;
};



// 全局变量
export function getBaseURL(){
  return "http://localhost:8080/fusion"
  // return "http://172.31.21.36:8080/fusion"
  if(window._wefeApi){
      /** 提供给客户快速修改请求地址，一般通过修改html head */
      return window._wefeApi;
  }
  return window.location.origin
  // return  `${process.env[`VUE_APP_${process.env.HOST_ENV}`]}${second ? `-${second}` : ''}`;
}

export const request = axiosInstance({
  message,
  baseURL:getBaseURL(),
  invalidTokenCodes:['401'],
  successCode:0,
  onTokenInvalid: generateOnTokenInvalid(message, getTokenName()),
  getHeaders: () => ({
    'x-user-token': getTokenByName(getTokenName()),
    'x-req-rd': createUUID(),
    'x-req-timestamp': formatDate(),
  }),
});
export const getToken = ()=>getTokenByName(getTokenName())


