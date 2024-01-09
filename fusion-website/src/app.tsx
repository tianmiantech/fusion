import moment  from "moment";
import React, { useEffect } from 'react';
import { useLocation, history } from 'umi';
import 'moment/locale/zh-cn';
moment.locale('zh-cn');

export async function getInitialState(): Promise<{ name: string }> {
    return { name: '@umijs/max' };
}
const App: React.FC = ({ children }) => {
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const redirectParam = queryParams.get('redirect');
  console.log('======App ======');
  console.log('redirectParam',redirectParam);
  
  useEffect(() => {
    if (redirectParam) {
      // 在实际应用中，你可能需要进行一些验证或处理
      // 这里简单地进行跳转
      history.push(redirectParam);
    }
  }, [redirectParam]);

  return <div>{children}</div>;
};

export default App;