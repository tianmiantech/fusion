import {useRef} from 'react'
import { Table,Tag ,Button,Card, message, Space,Spin, Badge} from "antd"
import { TmTable } from "@tianmiantech/pro";
import type { ColumnsType } from 'antd/es/table';
import { useRequest,useMount } from "ahooks";
import { history } from 'umi';
import { useModel } from '@umijs/max';
import { useImmer } from 'use-immer';
import styles from './index.less'
import { getJobList,deleteJob,restartJob } from "../service";
import moment from "moment";
import {dataResourceTypeMap,AddMethodMap,JobStatus,JOB_STATUS,ROLE_TYPE} from '@/constant/dictionary'
import lodash from 'lodash'
import {getPersonificationTime} from '@/utils/time'
import {renderHashConfig} from '@/utils/utils'

export interface RowProps {
    created_time:number,
    role:'promoter'|'provider',
    creator_member_id:string,
    remark:string,
    partner:string,
    id:string,
    myself:{
        created_time:number,
        data_resource_type:string,
        id:string,
        role:'promoter'|'provider',
        table_data_resource_info:Map<string,any>,
        total_data_count:number,
        updated_time:number
    },
    updated_time:string,
    status:string
}

interface ActionItemInterface {
    text:string,
    key:string,
    confirmConfig?:any
}

const ROLE_TO_CN = {
    [ROLE_TYPE.PROMOTER]:'我发起的',
    [ROLE_TYPE.PROVIDER]:'我参与的',
 }

const Index =()=>{

    const tabelRef = useRef<any>()
   // 角色类型

   
    const [jobListData,setJobListData] = useImmer({
        page_size:10,
        page_index:0,
        total:0,
        dataSource:[],
        isFirstLoading:true//标记是否第一次加载,用于判断是否显示loading
    })

    const {run:runGetJobListData,loading:getJobListLoading} = useRequest(async (params)=>{
        const reponse = await getJobList(params)
        const {code,data} = reponse;
        if (code == 0) {
            setJobListData(draft=>{
                const list = lodash.get(data,'list',[])
                const total = lodash.get(data,'total',0)
                draft.total = total;
                draft.isFirstLoading = false;
                draft.dataSource = list;

            })
            
        }
    },{manual:true})

    const {run:deleteJobData,loading:deleteLoading} = useRequest(async (id)=>{
        const reponse = await deleteJob(id)
        const {code,data} = reponse;
        if (code == 0) {
            message.success('删除成功')
            runGetJobListData({page_size:jobListData.page_size,page_index:jobListData.page_index,role:''})
        }
    },{manual:true})

    const {run:restartJobData,loading:restartLoading} = useRequest(async (id)=>{
        const reponse = await restartJob(id)
        const {code,data} = reponse;
        if (code == 0) {
            message.success('重启成功')
            runGetJobListData({page_size:jobListData.page_size,page_index:jobListData.page_index})
        }
    },{manual:true})


    useMount(()=>{
        runGetJobListData({page_size:jobListData.page_size,page_index:jobListData.page_index,role:''})
    })


    const renderPromoterData = (row:RowProps,dataObj:any)=>{
        const {status,role} = row;
        return renderMaindData(row,dataObj)
    }

    const renderProviderData = (row:RowProps,dataObj:any)=>{
        const {status,role} = row;
        if( status === JOB_STATUS.AUDITING ){
          return renderPartnerUrls(row,dataObj)
        } else {
          return renderMaindData(row,dataObj)
        }
    }

    const renderMaindData = (row:RowProps,dataObj:any)=>{
        if (!dataObj) {
            return <>暂无内容</>
        }
        const { data_resource_type,total_data_count,hash_config} = dataObj||{};
        return <>
            <div>数据类型/数据量：{dataResourceTypeMap.get(data_resource_type)}/{total_data_count}</div>
            <div>主键：{renderHashConfig(hash_config)}</div>
        </>
    }

    const renderPartnerUrls = (row:RowProps,dataObj:any)=>{
        if (!dataObj) {
            return <>暂无内容</>
        }
        const member_name = lodash.get(dataObj,'member_name')
        const base_url  = lodash.get(dataObj,'base_url')
        return <>{member_name && <div>协作方名称：{member_name}</div>}
        <div>服务地址：{base_url}</div>
        </>
    }


    const getBadgeStatus = (status:string)=>{
        if (status === JOB_STATUS.RUNNING) {
            return 'processing';
        } else if (status === JOB_STATUS.SUCCESS) {
            return 'success';
        } else if (status === JOB_STATUS.ERROR_ON_RUNNING) {
            return 'error';
        } else if (status === JOB_STATUS.AUDITING) {
            return 'warning';
        } 
        else {
            return 'default';
        }
    }
    

    const columns: ColumnsType<RowProps>|any = [{
        title: '我方角色/创建时间',
        dataIndex: 'create',
        key: 'create',
        width:200,
        render:(text:string,row:RowProps)=>{
            const { created_time = new Date().getTime(),role} = row;
            return <><Tag color={role==='promoter'?'success':'blue'}>{ROLE_TO_CN[`${role}`]}</Tag>
            <div>{getPersonificationTime(created_time)}</div>
            </>
        }
    },{
        title: '发起方',
        dataIndex: 'promoter',
        key: 'promoter',
        width:400,
        render:(text:string,row:RowProps)=>{
          const { role,id } = row;
          //role表示我方这条数据中所处的角色 
          //展示发起方的数据表示 我方在当前数据中为发起方,则取myself字段，否则取partner字段
          const key = role===ROLE_TYPE.PROMOTER?'myself':'partner';
          const dataObj = lodash.get(row,key,null);
          return renderPromoterData(row,dataObj)
        }
    },{
        title: '协作方',
        dataIndex: 'provider',
        key: 'provider',
        width:400,
        render:(text:string,row:RowProps)=>{
          const { role,id } = row;
          const key = role===ROLE_TYPE.PROVIDER?'myself':'partner';
          const dataObj = lodash.get(row,key,null);
          return renderProviderData(row,dataObj)
        }
    },{
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width:80,
        render:(text:string)=>{
            return <Badge status={getBadgeStatus(text)}  text={JobStatus.get(text)}/>
        }
    },{
        title: '备注',
        dataIndex: 'remark',
        key: 'remark'
    },{
        key: 'optionOop',
        title: '操作',
        fixed: 'right',
        width:120,
        render:(record:RowProps)=> {
            const {role,status} = record;
            const defaultList = [<Button type='link' onClick={()=>{actionClickHandle('detail',record)}}>{role===ROLE_TYPE.PROVIDER&& status === JOB_STATUS.AUDITING?'去审批':'详情'}</Button>]
            if(role === ROLE_TYPE.PROMOTER){
                if(!status || status === JOB_STATUS.EDITING){
                    defaultList.push(<Button type='link' onClick={()=>{actionClickHandle('delete',record)}}>删除</Button>)
                }
                if(status === JOB_STATUS.SUCCESS|| status === JOB_STATUS.STOP_ON_RUNNING|| status === JOB_STATUS.ERROR_ON_RUNNING  ){
                    defaultList.push(<Button type='link'  onClick={()=>{actionClickHandle('restart',record)}}>重启任务</Button>)
                }
            }
            return <Space>
                {defaultList}
            </Space>
        },
    }]

    const actionClickHandle = (key:string, record:RowProps,)=>{
        if (key === 'detail') {
            const {id} = record
            history.push(`/job/detail/${id}`);
        } else if(key === 'delete'){
            const {id} = record;
            deleteJobData(id)
        } else if(key === 'restart'){
            const {id} = record;
            restartJobData(id)
        }
    }

    const renderBtn = ()=>{
        return <Button type="primary" onClick={()=>{
            history.push('/job/create')
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

    const renderLoading = ()=>{
        return <div className={styles.container}>
        <Spin size='large'/>
        </div>
    }

    const renderList = ()=>{
        return <Card title='任务列表' extra={renderBtn()}>
                <TmTable
                    ref={tabelRef}
                    dataSource={jobListData.dataSource}
                    columns={columns}
                    pagination={{
                        page_size:jobListData.page_size,
                        current:jobListData.page_index,
                        showSizeChanger: false,
                        size: 'small',
                    }}
                >
                <TmTable.Table
                    loading={getJobListLoading||deleteLoading||restartLoading}
            />
        </TmTable>
        </Card>
    }

    const renderContent = ()=>{ 
      if(jobListData.isFirstLoading){
        return renderLoading()
      } else if(jobListData.dataSource.length === 0){
        return renderNoData()
      } else {
        return renderList()
      }
    }

    return <>
      {renderContent()}
      </>
    }
export default Index