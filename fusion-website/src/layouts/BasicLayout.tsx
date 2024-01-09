
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet,history,useModel} from '@umijs/max';
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
              <Layout style={{width:'100%',height:'100%'}}>
                  <Header/>
                  <Content style={{overflowY:'auto'}}>
                    <Outlet/>
                  </Content>
              </Layout>
    </ConfigProvider>
}
export default BasicLayout;