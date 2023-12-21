import {useRef} from 'react'
import { Layout, Space } from 'antd';
import styles from './index.less'
import {SettingOutlined,PlusOutlined,BarsOutlined } from "@ant-design/icons"
import GlobalConfigDrawer from '../GlobalConfigDrawer'
import HistoryListDrawer from '../HistoryListDrawer'
import { useLocation,history } from 'umi';
const { Header, Footer, Sider, Content } = Layout;
const Index =()=>{
    const secretKeyDrawerRef:any = useRef()
    const historyListRef:any  = useRef()
    const location = useLocation();

    const renderSetting = ()=>{
       return  <SettingOutlined className={styles.setting} onClick={()=>{secretKeyDrawerRef?.current?.showDrawer()}}/>
    }

    const showHistory=()=>{
        historyListRef?.current?.showDrawer()
    }
    
    const renderRightContent = ()=>{
        if (location.pathname.indexOf('/home')==-1) {
            return <>
                <PlusOutlined  className={styles.setting} onClick={()=>{history.push('/job/create')}}/>
                <BarsOutlined  className={styles.setting} onClick={showHistory}/>
                {renderSetting()}
            </>
        } 
        return renderSetting()
    }

    

    return <Header className={styles.layoutHeader}>
        <div className={styles.headerBar}>
            <div className={styles.left} onClick={()=>{
                history.replace('/home')
            }}>
                主页
            </div>
            <div className={styles.right}>
                {renderRightContent()}
            </div>
        </div>
        <GlobalConfigDrawer ref={secretKeyDrawerRef}/>
        <HistoryListDrawer ref={historyListRef}/>
    </Header>
}
export default Index