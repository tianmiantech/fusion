import { Button, Card } from 'antd'
import styles from './index.less'
import History from './History'
import { isQianKun } from '@/utils/request'

const Index = ()=>{

    const renderBtn = ()=>{
        return <Button type="primary">发起任务</Button>
    }

    const renderNoData = ()=>{
        return <div className={styles.container}>
        <h3>欢迎使用数据融合工具</h3>
        <div className={styles.btnStyle} >
            {renderBtn()}
        </div>
        </div>
    }

    

    return <Card title='历史任务' extra={renderBtn()}>
        <History/>
    </Card>
   
}
export default Index