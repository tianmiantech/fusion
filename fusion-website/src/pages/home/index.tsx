import { Button, Card } from 'antd'
import styles from './index.less'
import History from './History'
import { isQianKun } from '@/utils/request'
import { history } from 'umi';
import { useMount } from 'ahooks';
import { useState } from 'react';
import {useRequest,} from 'ahooks'
import {checkIsInitialized} from '../../models/service'
import lodash from 'lodash'


const Index = ()=>{    

    const renderBtn = ()=>{
        return <Button type="primary" onClick={()=>{
            history.push('/task')
        }}>发起任务</Button>
    }

    const renderNoData = ()=>{
        return <div className={styles.container}>
        <h3>欢迎使用数据融合工具</h3>
        <div className={styles.btnStyle} >
            {renderBtn()}
        </div>
        </div>
    }

    const renderList = ()=>{
        return <Card title='历史任务' extra={renderBtn()}>
        <History/>
    </Card>
    }

    return renderNoData()
   
}
export default Index