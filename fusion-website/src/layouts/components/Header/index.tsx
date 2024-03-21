import {useRef} from 'react'
import { Layout, Badge } from 'antd';
import styles from './index.less'
import {SettingOutlined,PlusOutlined,BarsOutlined,MailOutlined} from "@ant-design/icons"
import GlobalConfigDrawer from '../GlobalConfigDrawer'
import HistoryListDrawer from '../HistoryListDrawer'
import AuditingListDrawer from '../AuditingListDrawer';
import { useLocation,history } from 'umi';
const { Header, Footer, Sider, Content } = Layout;
const Index =()=>{
    const secretKeyDrawerRef = useRef<any>()
    const historyListRef  = useRef<any>()
    const location = useLocation();

    const showHistory=()=>{
        historyListRef?.current?.showDrawer()
    }
    
    const renderRightContent = ()=>{
        const iconList = [<AuditingListDrawer key={'AuditingListDrawer'}/>]
        if (location.pathname.indexOf('/home')==-1 && location.pathname.indexOf('/job/create')==-1) {
            iconList.push(<PlusOutlined key={'PlusOutlined'}  className={styles.setting} onClick={()=>{history.push('/job/create')}}/>)
            // iconList.push(<BarsOutlined key={'BarsOutlined'} className={styles.setting} onClick={showHistory}/>)
        }
        iconList.push(<SettingOutlined key={'SettingOutlined'} className={styles.setting} onClick={()=>{secretKeyDrawerRef?.current?.showDrawer()}}/>)
        return <>{iconList}</>
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