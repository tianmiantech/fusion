
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet,history,useModel} from '@umijs/max';
import { TmLayout } from '@tianmiantech/pro';
import zhCN from 'antd/es/locale/zh_CN';
import Header from './components/Header'
import { useMount } from "ahooks";
interface BasicLayoutPros {
  children:any
}

const { Footer, Sider, Content } = Layout;

const BasicLayout = (props:BasicLayoutPros) => {

  const {checkInitialize} = useModel("initializeConfig");
  
  useMount(()=>{
    //检查系统是否被初始化 、、
    checkNeedInitialize()
  })

  const checkNeedInitialize = async ()=>{
    const isInitialize = await checkInitialize();
    //如果没有初始化，则进行用户初始化
    if(!isInitialize){
      history.push("/register")
    }
  }

  return  <ConfigProvider prefixCls={'fusion'}  locale={zhCN}>
              <TmLayout.Container>
                   <Header/>
                  <TmLayout.Main >
                    <TmLayout.Content style={{ overflow:'auto', height:"calc(100vh - 40px)", padding: '14px 12px'}}>
                        <Outlet/>
                    </TmLayout.Content>
                  </TmLayout.Main>
              </TmLayout.Container>
    </ConfigProvider>
}
export default BasicLayout;