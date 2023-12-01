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

interface RowProps {
    createTime:number,
    role:'prompter'|'provider',
    remark:string,
    partner:string,
    myself:{
        created_time:number,
        data_resource_type:string,
        id:string,
        role:'prompter'|'provider',
        table_data_resource_info:Map<string,any>,
        total_data_count:number,
        updated_time:number
    },
    updated_time:string,
    status:string
}

const Index =()=>{

    // 角色类型
    const RoleMap = new Map([
        ['prompter', '我发起的'],
        ['provider', '我协作的'],
    ])
   
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
            console.log("data",data);
            
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
            return <><div> <Tag color={role==='prompter'?'success':'default'}>{RoleMap.get(role)}</Tag></div>
            <div>{moment(createTime).startOf('hour').fromNow()}</div>
            </>
        }
    },{
        title: '我方数据',
        dataIndex: 'myself',
        key: 'myself',
        width:400,
        render:(text:string,row:RowProps)=>{
            const { myself } = row;
           return<>22</>
        }
    },{
        title: '协作方',
        dataIndex: 'provider',
        key: 'provider',
        width:400,
        render:(text:string,row:RowProps)=>{
            return <>22</>
        }
    },{
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width:80,
        render:(text:string)=>{
            return <Tag color="success">{text}</Tag>
        }
    }]

    const actionClickHandle = (key:string, record:RowProps, index:number)=>{
        if (key === 'detail') {
            history.push('/task/detail');
        }
    }

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