
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet} from '@umijs/max';
import { TmLayout } from '@tianmiantech/pro';
import Header from './components/Header'
interface BasicLayoutPros {
  children:any
}

const { Footer, Sider, Content } = Layout;

const BasicLayout = (props:BasicLayoutPros) => {
 

 

  return  <>
          <TmLayout.Container>
                <Header/>
              <TmLayout.Main >
                <TmLayout.Content style={{ overflow:'auto', height:"calc(100vh - 40px)", padding: '14px 12px'}}>
                    <Outlet/>
                </TmLayout.Content>
              </TmLayout.Main>
          </TmLayout.Container>
        </>
}
export default BasicLayout;