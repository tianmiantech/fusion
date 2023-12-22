import React,{useState,useImperativeHandle,forwardRef} from 'react'
import { Button, Table } from 'antd'
import { Layout, Badge } from 'antd';
import {SettingOutlined,PlusOutlined,BarsOutlined,MailOutlined} from "@ant-design/icons"
import { TmDrawer } from '@tianmiantech/pro';
import { useRequest,useMount } from 'ahooks';
import { useImmer } from 'use-immer';
import lodash from 'lodash'
import { getJobList } from '@/pages/home/service';
import type {GetJobListRequestInterface} from '@/pages/home/service'
import styles from './index.less'
import { ROLE_TYPE,JOB_STATUS } from '@/constant/dictionary';

interface AuditingListDrawerProps {
    // Add props here
}

const AuditingListDrawer: React.FC<AuditingListDrawerProps> = forwardRef((props,ref) => {

    const [auditData, setAuditData] = useImmer({
        visible:false,
        showDot:false,
        auditList:[]
    });

    const {run:runGetJobList} = useRequest(async ()=>{
        const requestParams = {
          page_size:1000,
          status:JOB_STATUS.AUDITING,
          page_index:0,
          role:ROLE_TYPE.PROVIDER
        } as GetJobListRequestInterface
        const res = await getJobList(requestParams)
        const {code,data} = res;
        if(code === 0){
            const listData = lodash.get(data,'list',[])
            const resultData= listData.map((item:any)=>{
                const partner = lodash.get(item,'partner',{})
                const created_time = lodash.get(item,'created_time','')
                const updated_time = lodash.get(item,'updated_time','')
                return {
                    ...partner,
                    created_time:updated_time || created_time,
                }
            })
            setAuditData(g=>{
                g.showDot = listData.length>0
                g.auditList = resultData
            })
        }
    },{pollingInterval:3000})

    const showDrawer = (values:boolean)=>{
        setAuditData(g=>{
            g.visible = values
        })
    }

    useImperativeHandle(ref,()=>{
        return {
            showDrawer:showDrawer
        }
    })

    const columnsList = [{
      title:'合作方名称',
      dataIndex:'member_name'
    },{
      title:'合作方地址',
      dataIndex:'base_url'
    },{
      title:'发起时间',
      dataIndex:'created_time'
    },{
      title:'操作',
      dataIndex:'action',
      render:()=>(
        <>
        <Button type="link">去审核</Button>
        </>
      )
    }]

    return <>
    <Badge dot={auditData.showDot}  className={styles.msgStyle} ><MailOutlined style={{color:'white'}} onClick={()=>showDrawer(true)}/></Badge>
    <TmDrawer 
        title={'待处理任务'} 
        onClose={()=>{
            showDrawer(false)
        }} 
        width={700}
        open={auditData.visible}
        footer={null}
       >
        <Table columns={columnsList} dataSource={auditData.auditList}/>
       </TmDrawer>
    </>
});

export default AuditingListDrawer;
