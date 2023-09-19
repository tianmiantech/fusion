
import styles from './index.less';
import { ConfigProvider, Drawer, Layout } from 'antd';
import { Outlet } from 'umi';
interface BasicLayoutPros {
  children:any
}
const BasicLayout = (props:BasicLayoutPros) => {
  return <ConfigProvider><Outlet/></ConfigProvider>
}
export default BasicLayout;