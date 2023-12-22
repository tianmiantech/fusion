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
    const secretKeyDrawerRef:any = useRef()
    const historyListRef:any  = useRef()
    const location = useLocation();

    const showHistory=()=>{
        historyListRef?.current?.showDrawer()
    }
    
    const renderRightContent = ()=>{
        const iconList = [<AuditingListDrawer/>]
        if (location.pathname.indexOf('/home')==-1) {
            iconList.push(<PlusOutlined  className={styles.setting} onClick={()=>{history.push('/job/create')}}/>)
            iconList.push(<BarsOutlined  className={styles.setting} onClick={showHistory}/>)
        }
        iconList.push(<SettingOutlined className={styles.setting} onClick={()=>{secretKeyDrawerRef?.current?.showDrawer()}}/>)
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