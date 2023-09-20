
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet } from 'umi';
import Header from './components/Header'
interface BasicLayoutPros {
  children:any
}

const { Footer, Sider, Content } = Layout;

const BasicLayout = (props:BasicLayoutPros) => {
  return <ConfigProvider>
      <Layout style={{width:'100%',height:'100%'}}>
          <Header/>
          <Content >
            <Outlet/>
          </Content>
      </Layout>
  
    </ConfigProvider>
}
export default BasicLayout;