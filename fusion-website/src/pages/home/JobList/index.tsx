import { Table,Tag ,Button,Card} from "antd"
import { TmTable } from "@tianmiantech/pro";
import type { ColumnsType } from 'antd/es/table';
import { useRequest,useMount } from "ahooks";
import { history } from 'umi';
import { useModel } from '@umijs/max';
import { useImmer } from 'use-immer';
import styles from './index.less'
import { getJobList } from "../service";
import moment from "moment";
import {dataResourceTypeMap,AddMethodMap,JobStatus} from '@/constant/dictionary'
import lodash from 'lodash'

interface RowProps {
    createTime:number,
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

const ROLE_TO_CN = {
    'promoter':'我发起的',
    'provider':'我参与的',
 }

const Index =()=>{

   // 角色类型

   
    const [jobListData,setJobListData] = useImmer({
        page_size:10,
        page_index:0,
        total:0,
        dataSource:[]
    })

    const {run:runGetJobListData,loading:getJobListLoading} = useRequest(async (params)=>{
        const reponse = await getJobList(params)
        const {code,data} = reponse;
        if (code == 0) {
            setJobListData(draft=>{
                const list = lodash.get(data,'list',[])
                draft.dataSource = list;
            })
            
        }
    },{manual:true})


    useMount(()=>{
        runGetJobListData({page_size:jobListData.page_size,page_index:jobListData.page_index,role:''})
    })






    const columns: ColumnsType<RowProps> = [{
        title: '任务角色/创建时间',
        dataIndex: 'create',
        key: 'create',
        width:200,
        render:(text:string,row:RowProps)=>{
            const { createTime = new Date(),role} = row;
            console.log("role",role);
            console.log("ROLE_TO_CN",ROLE_TO_CN[role]);
            
            console.log("role-",ROLE_TO_CN[`${role}`]);
            
            return <><Tag color={role==='promoter'?'success':'default'}>{ROLE_TO_CN[`${role}`]}</Tag>
            <div>{moment(createTime).startOf('hour').fromNow()}</div>
            </>
        }
    },{
        title: '我方',
        dataIndex: 'myself',
        key: 'myself',
        width:400,
        render:(text:string,row:RowProps)=>{
            const { myself } = row;
            if(!myself)
                return<>暂无内容</>
            else {
                const { data_resource_type,table_data_resource_info,total_data_count,updated_time } = myself||{};
                const add_method = lodash.get(table_data_resource_info,'add_method')
                return <>
                    <div>数据类型/数据量：{dataResourceTypeMap.get(data_resource_type)}/{total_data_count}</div>
                    <div>数据来源/更新时间：{AddMethodMap.get(add_method)}/{moment(updated_time).startOf('hour').fromNow()}</div>
                </>
            }
        }
    },{
        title: '协作方',
        dataIndex: 'provider',
        key: 'provider',
        width:400,
        render:(text:string,row:RowProps)=>{
            return <>暂无内容</>
        }
    },{
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width:80,
        render:(text:string)=>{
            return <Tag >{JobStatus.get(text)}</Tag>
        }
    }]

    const actionClickHandle = (key:string, record:RowProps, index:number)=>{
        if (key === 'detail') {
            console.log("record",record);
            const {id} = record
            history.push(`/job/detail/${id}`);
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

    const renderList = ()=>{
        return <Card title='任务列表' extra={renderBtn()}>
            <TmTable
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
                    actionItems={(record:RowProps) => [
                    { text: '详情', key: 'detail'},
                    { text: '删除', key: 'delete' },
                    { text: '查看备注', key: 'remark' },
                    ]}
                actionClickHandle={actionClickHandle}
            />
        </TmTable>
        </Card>
    }

    return <>
    {jobListData.dataSource.length>0?renderList():renderNoData()}
    </>

}
export default Index