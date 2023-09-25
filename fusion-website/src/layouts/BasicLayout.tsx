
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet } from 'umi';
import Header from './components/Header'
import { TmLayout, TmContext } from '@tianmiantech/pro';

interface BasicLayoutPros {
  children:any
}

const { Footer, Sider, Content } = Layout;

const BasicLayout = (props:BasicLayoutPros) => {
  return <ConfigProvider prefixCls={'fusion'}  >
    <TmContext.Provider value={{ prefixCls: 'fusion' }}>
        <TmLayout.Provider value={{ prefixCls: 'fusion' }}>
      <Layout style={{width:'100%',height:'100%'}}>
          <Header/>
          <Content >
            <Outlet context={{ prefixCls: 'fusion'}}/>
          </Content>
      </Layout>
      </TmLayout.Provider>
      </TmContext.Provider>
    </ConfigProvider>
}
export default BasicLayout;