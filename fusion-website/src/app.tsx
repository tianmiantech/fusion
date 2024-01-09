import moment  from "moment";
import React, { useEffect } from 'react';
import { useLocation, history } from 'umi';
import {getReactRouter} from '@/utils/utils'
import 'moment/locale/zh-cn';
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
  const location = window.location;
  const {children} = props;

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const redirectParam = queryParams.get('redirect')||'';
    const router = getReactRouter(redirectParam)
    console.log('router',router);
    if(location.pathname.includes('index.html') && redirectParam ) {
      history.push(router);
    }
  }, [location.search]);

  return <>{children}</>;
};

export function rootContainer(lastRootContainer:any, args:any) {
  return React.createElement(App,null,lastRootContainer);
}