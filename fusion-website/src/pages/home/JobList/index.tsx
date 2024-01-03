import {useRef} from 'react'
import { Table,Tag ,Button,Card, message, Space,Spin, Badge,Form,Radio } from "antd"
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
    algorithm:string,
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

interface JobListPropsInterface {
    handleCallBack?:Function
}

const Index =(props:JobListPropsInterface)=>{


    const {handleCallBack} = props;

    const tabelSearchFormRef = useRef<any>()
   // 角色类型

   
    const [jobListData,setJobListData] = useImmer({
        page_size:10,
        page_index:1,
        total:0,
        dataSource:[],
        isFirstLoading:true,//标记是否第一次加载,用于判断是否显示loading
        isSearch:false //标记是否是搜索
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
        runGetJobListData({page_size:jobListData.page_size,page_index:jobListData.page_index})
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
            <div>资源：{dataResourceTypeMap.get(data_resource_type)}/{total_data_count}</div>
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
        title: '角色',
        dataIndex: 'role',
        key: 'role',
        hideInTable:true,
        formItemProps:{
          labelCol:2
        },
        valueType: 'radioButton',
        valueEnum: {
          [ROLE_TYPE.PROMOTER]: { text: '我发起的' },
          [ROLE_TYPE.PROVIDER]: { text: '我参与的' },
        },
    },{
        title: '任务ID',
        dataIndex: 'job_id',
        key: 'job_id',
        hideInTable:true,
        fieldProps: {
          style:{
            width:250
          }
        },
        formItemProps:{
          labelCol:2
        },
    },
      {
        title: '时间/算法',
        dataIndex: 'create',
        key: 'create',
        search: false,
        width:200,
        render:(text:string,row:RowProps)=>{
            const { created_time = new Date().getTime(),role,algorithm} = row;
            return <><Tag color={role==='promoter'?'success':'blue'}>{ROLE_TO_CN[`${role}`]}</Tag>
            <div>{algorithm}/{getPersonificationTime(created_time)}</div>
            </>
        }
    },{
        title: '发起方',
        dataIndex: 'promoter',
        key: 'promoter',
        search: false,
        width:300,
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
        search: false,
        width:300,
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
        width:90,
        valueType: 'select',
        valueEnum: JobStatus,
        fieldProps: {
          style:{
            width:120
          }
        },
        render:(text:string,row:RowProps)=>{
          const status = lodash.get(row,'status',null);
          return <Badge status={getBadgeStatus(status)}  text={JobStatus.get(status)}/>
        }
    },{
        title: '备注',
        search: false,
        dataIndex: 'remark',
        key: 'remark'
    },{
        key: 'optionOop',
        title: '操作',
        fixed: 'right',
        search: false,
        width:120,
        render:(record:RowProps)=> {
            const {role,status} = record;
            const defaultList = [<Button key={'auditing'} type='link' onClick={()=>{actionClickHandle('detail',record)}}>{role===ROLE_TYPE.PROVIDER&& status === JOB_STATUS.AUDITING?'去审批':'详情'}</Button>]
            if(role === ROLE_TYPE.PROMOTER){
                if(!status || status === JOB_STATUS.EDITING){
                    defaultList.push(<Button key={'detail'} type='link' onClick={()=>{actionClickHandle('delete',record)}}>删除</Button>)
                }
                if(status === JOB_STATUS.SUCCESS|| status === JOB_STATUS.STOP_ON_RUNNING|| status === JOB_STATUS.ERROR_ON_RUNNING  ){
                    defaultList.push(<Button key={'restart'} type='link'  onClick={()=>{actionClickHandle('restart',record)}}>重启任务</Button>)
                }
            }
            return <Space>
                {defaultList}
            </Space>
        },
    }]

    const actionClickHandle = (key:string, record:RowProps,)=>{
        handleCallBack && handleCallBack(record)
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
            handleCallBack && handleCallBack()
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
    
    const searchData = async (page_index=1)=>{
        const values = await tabelSearchFormRef.current?.getFieldsValue();
        runGetJobListData({page_size:jobListData.page_size,page_index,...values})
        setJobListData(draft=>{
            draft.isSearch = true;
            if(page_index!==jobListData.page_index){
              draft.page_index = page_index;
            }
            
        })
    }
    
    
    const renderList = ()=>{
        return <TmTable
                  dataSource={jobListData.dataSource}
                  columns={columns}
                  rowKey="id"
                >
                <TmTable.Table
                    loading={getJobListLoading||deleteLoading||restartLoading}
                    cardBordered
                    formRef={tabelSearchFormRef}
                    pagination={{
                      pageSize:jobListData.page_size,
                      current:jobListData.page_index,
                      size: 'small',
                      total:jobListData.total,
                      onChange: (page:number, pageSize:number) => {
                        searchData(page)
                      }
                    }}
                    search={{
                      span: 6,
                      optionRender: (searchConfig:any, formProps:any) => <Space>
                        <Button key='resetFields' onClick={() => {
                          tabelSearchFormRef.current?.resetFields();
                        }}>重置</Button>
                        <Button  key='submit' type="primary" onClick={() => {
                        searchData()
                        }}>搜索</Button>
                        {renderBtn()}
                      </Space>
                    }}
            />
        </TmTable>
    }

    const renderContent = ()=>{ 
      if(jobListData.isFirstLoading){
        return renderLoading()
      } else if(jobListData.dataSource.length === 0 && !jobListData.isSearch){
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