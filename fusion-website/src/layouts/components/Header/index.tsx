import {useRef} from 'react'
import { Layout, Space } from 'antd';
import styles from './index.less'
import {SettingOutlined } from "@ant-design/icons"
import SecretKeyDrawer from '../SecretKeyDrawer'

const { Header, Footer, Sider, Content } = Layout;
const Index =()=>{
    const secretKeyDrawerRef:any = useRef()
    

    return <Header className={styles.layoutHeader}>
        <div className={styles.headerBar}>
            <div className={styles.left}>
                
            </div>
            <div className={styles.right}>
                <SettingOutlined className={styles.setting} onClick={()=>{secretKeyDrawerRef?.current?.showDrawer()}}/>
            </div>
        </div>
        <SecretKeyDrawer ref={secretKeyDrawerRef}/>
    </Header>
}
export default Index