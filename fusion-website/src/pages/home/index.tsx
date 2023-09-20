import { Button } from 'antd'
import styles from './index.less'
const Index = ()=>{

    return <div className={styles.container}>
        <h3>欢迎使用数据融合工具</h3>
        <Button type="primary">发起任务</Button>
    </div>
}
export default Index