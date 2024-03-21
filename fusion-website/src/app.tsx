import moment  from "moment";
import { ConfigProvider, Drawer, Layout } from 'antd';
import React, { useEffect } from 'react';
import { history } from 'umi';
import {getReactRouter} from '@/utils/utils'
import zhCN from 'antd/es/locale/zh_CN';
import useCheckInitializedStore from './hooks/useCheckInitializedStore';
import 'moment/locale/zh-cn';
import { useMount } from "ahooks";
moment.locale('zh-cn');

export async function getInitialState(): Promise<{ name: string }> {
    return { name: '@umijs/max' };
}
// export function render(oldRender:any) {
//   const location = window.location;
//   const queryParams = new URLSearchParams(location.search);
//   const redirectParam = queryParams.get('redirect');
//   console.log('redirectParam',redirectParam);
//   oldRender()
// }


const App: React.FC = (props:any) => {

  const {checkInitialize,IsInitialized} = useCheckInitializedStore();
  const location = window.location;
  const {children} = props;

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const redirectParam = queryParams.get('redirect')||'';
    console.log('redirectParam',redirectParam);
    
    const router = getReactRouter(redirectParam)
    console.log('router',router);
    console.log('location.pathname',location.pathname);
    
    if(location.pathname.includes('index.html') && redirectParam ) {
      history.push(`/${router}`);
    }
  }, [location.search]);

  useMount(()=>{
    //检查系统是否被初始化 、、
    checkInitialize()
  })

  useEffect(()=>{
    if(!IsInitialized){
      history.push("/register")
    } else if(location.pathname.includes('/register')){
      history.push("/home")
    }
  },[IsInitialized])


  return <ConfigProvider prefixCls={'fusion'}  locale={zhCN}>
    {children}</ConfigProvider>;
};

export function rootContainer(lastRootContainer:any, args:any) {
  return React.createElement(App,null,lastRootContainer);
}