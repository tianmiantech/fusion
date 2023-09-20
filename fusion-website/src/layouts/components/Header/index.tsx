import { Layout, Space } from 'antd';
import styles from './index.less'
import {SettingOutlined } from "@ant-design/icons"
const { Header, Footer, Sider, Content } = Layout;
const Index =()=>{
    return <Header className={styles.layoutHeader}>
        <div className={styles.headerBar}>
            <div className={styles.left}>
                
            </div>
            <div className={styles.right}>
            <SettingOutlined />
            </div>
        </div>
    </Header>
}
export default Index