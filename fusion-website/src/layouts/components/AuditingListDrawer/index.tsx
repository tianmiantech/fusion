import React,{useState,useImperativeHandle,forwardRef} from 'react'
import { Button, Table } from 'antd'
import { Layout, Badge } from 'antd';
import { history } from 'umi';
import {SettingOutlined,PlusOutlined,BarsOutlined,MailOutlined} from "@ant-design/icons"
import { TmDrawer } from '@tianmiantech/pro';
import { useRequest,useMount } from 'ahooks';
import { useImmer } from 'use-immer';
import lodash from 'lodash'
import { getAuditingList } from './service';
import styles from './index.less'
import { ROLE_TYPE,JOB_STATUS } from '@/constant/dictionary';
import {getPersonificationTime} from '@/utils/time'
import type {RowProps} from '@/pages/home/JobList/index'
interface AuditingListDrawerProps {
    // Add props here
}

const AuditingListDrawer: React.FC<AuditingListDrawerProps> = forwardRef((props,ref) => {

    const [auditData, setAuditData] = useImmer({
        visible:false,
        showDot:false,
        auditList:[],
    });

    const {run:runGetJobList} = useRequest(async ()=>{
        const res = await getAuditingList()
        const {code,data} = res;
        if(code === 0){
            const listData = lodash.get(data,'list',[])
            const resultData= listData.map((item:any)=>{
                const jobId = lodash.get(item,'id','')
                const partner = lodash.get(item,'partner',{})
                const created_time = lodash.get(item,'created_time','')
                const updated_time = lodash.get(item,'updated_time','')
                return {
                    ...partner,
                    created_time:updated_time || created_time,
                    jobId
                }
            })
            setAuditData(g=>{
                g.showDot = listData.length>0
                g.auditList = resultData
            })
        }
    },{pollingInterval:10000})

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

    const goAuditing = (row:RowProps)=>{
      const jobId = lodash.get(row,'jobId','')
      history.push(`/job/detail/${jobId}`);
      showDrawer(false)
    }

    const columnsList = [{
      title:'合作方名称',
      dataIndex:'member_name'
    },{
      title:'合作方地址',
      dataIndex:'base_url'
    },{
      title:'创建时间',
      dataIndex:'created_time',
      render:(text:number)=>{
        return <>{getPersonificationTime(text)}</>
      }
    },{
      title:'操作',
      dataIndex:'action',
      render:(text:string,row:RowProps)=>(
        <>
        <Button type="link" onClick={()=>{goAuditing(row)}}>去审批</Button>
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
