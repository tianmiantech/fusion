
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet} from '@umijs/max';
import Header from './components/Header'
interface BasicLayoutPros {
  children:any
}

const { Footer, Sider, Content } = Layout;

const BasicLayout = (props:BasicLayoutPros) => {
 
  return <Layout style={{height:'100%',paddingBottom:'40px'}}>
    <Header />
    <Content style={{ overflow: 'auto', height: "calc(100vh - 40px)", padding: '14px 12px' ,paddingBottom:'30px'}}>
      <Outlet />
    </Content>
  </Layout>
}
export default BasicLayout;