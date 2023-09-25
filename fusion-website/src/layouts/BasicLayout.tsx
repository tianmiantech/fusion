
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet } from 'umi';
import zhCN from 'antd/es/locale/zh_CN';
import Header from './components/Header'
import { TmLayout, TmContext } from '@tianmiantech/pro';

interface BasicLayoutPros {
  children:any
}

const { Footer, Sider, Content } = Layout;

const BasicLayout = (props:BasicLayoutPros) => {
  return <ConfigProvider prefixCls={'fusion'}  locale={zhCN}>
              <Layout style={{width:'100%',height:'100%'}}>
                  <Header/>
                  <Content >
                    <Outlet/>
                  </Content>
              </Layout>
    </ConfigProvider>
}
export default BasicLayout;