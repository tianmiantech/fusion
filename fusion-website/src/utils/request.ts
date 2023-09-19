import { message } from 'antd';
import { axiosInstance, iam } from '@tianmiantech/request';
import utils from '@tianmiantech/util';
import { getTokenName } from './index';

const { getToken, createUUID, formatDate } = utils;
const TOKEN = getTokenName();
const HOST_ENV = process.env.HOST_ENV;

const { generateOnTokenInvalid, invalidTokenCodes } = iam;

export const isQianKun = () => {
  return window.__POWERED_BY_QIANKUN__ || false;
};

export const getServiceName = () => {
  // if(!isQianKun()) return '';
  const { pathname } = window.location;
  const split = pathname.split('/') || [];
  return isQianKun() ? split[2] || '' : split[1] || '' ;
};


export const appCode = () => getServiceName() || 'board';
// 全局变量
export function baseURL(){
  const appCodes = appCode();
  const lastTwo = appCodes.substring(appCodes.length - 2);
  const second = /^\d+$/.test(lastTwo) ? lastTwo : '';

  if(window._wefeApi){
      /** 提供给客户快速修改请求地址，一般通过修改html head */
      return window._wefeApi;
  }
  return window.location.origin
  // return  `${process.env[`VUE_APP_${process.env.HOST_ENV}`]}${second ? `-${second}` : ''}`;
}

export const request = axiosInstance({
  message,
  baseURL:baseURL(),
  invalidTokenCodes,
  onTokenInvalid: generateOnTokenInvalid(message, TOKEN),
  getHeaders: () => ({
    'x-user-token': getToken(TOKEN),
    'x-req-rd': createUUID(),
    'x-req-timestamp': formatDate(),
  }),
});
