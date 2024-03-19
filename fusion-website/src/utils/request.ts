import { message } from 'antd';
import axiosInstance from './axios';
import { getTokenName, createUUID, sleep, formatDate } from './utils';
import { removeCookie, getToken as getTokenByName } from '@/utils/token';
import {FUNSION_INITIALIZED_KEY} from '@/constant/dictionary'





const HOST_ENV = process.env.HOST_ENV;



export const isQianKun = () => {
  return window.__POWERED_BY_QIANKUN__ || false;
};


export const extractFirstPathAfterOrigin = (url: string)=>{
  // 创建正则表达式，匹配 origin 后的第一个路径
  const regex = /^https?:\/\/[^\/]+\/([^\/]*)/;
  // 使用正则表达式匹配URL
  const match = url.match(regex);

  // 如果匹配成功，match数组的第一个元素是整个匹配，第二个元素是捕获组中的匹配部分
  if (match && match.length >= 2) {
    return match[1];
  } else {
    return '';
  }
}


// 全局变量
export function getRequestBaseURL(){
  //return "https://xbd-dev.tianmiantech.com/fusion-01"
  if(window._wefeApi){
      /** 提供给客户快速修改请求地址，一般通过修改html head */
      return window._wefeApi;
  }
  return `${window.location.origin}/${extractFirstPathAfterOrigin(window.location.href)}`;
}

export const request = axiosInstance({
  message,
  baseURL:getRequestBaseURL(),
  invalidTokenCodes:['10006'],
  successCode:0,
  infoCodes: [], // Add the missing infoCodes property
  whiteList: [], // Add the missing whiteList property
  onTokenInvalid: async (response:any) => {
    const { code, message: msg } = response.data;
    const initialized = localStorage.getItem(FUNSION_INITIALIZED_KEY)||'false';
    if (initialized==='false'|| window.location.pathname.match('/login')|| window.location.pathname.match('/register')) {
      return;
    }
    removeCookie(getTokenName())
    await sleep(1e3);
    const redirectUrl = location.href;
    const reLoginUrl = `login?redirect=${redirectUrl}`;
    
    location.href = `${window.location.origin}${process.env.BASE_PATH}${reLoginUrl}`;
      
  },
  getHeaders: () => ({
    'x-user-token': getTokenByName(getTokenName()),
    'x-req-rd': createUUID(),
    'x-req-timestamp': formatDate(),
  }),
});
export const getToken = ()=>getTokenByName(getTokenName())


