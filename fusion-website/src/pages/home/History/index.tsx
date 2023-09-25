import { Table,Tag ,Button} from "antd"
import { TmTable } from "@tianmiantech/pro";
import type { ColumnsType } from 'antd/es/table';
import { useRequest,useMount } from "ahooks";
import {getHistoryList} from './service'

interface RowProps {
    createTime:string,
    coast:string,
    prompterName:string,
    prompterFile:string,
    promptertotalNum:string,
    prompterprimaryKey:string,
    providerName:string,
    providerFile:string,
    providertotalNum:string,
    providerprimaryKey:string,
    intersection:string,
    status:string
}
const Index =()=>{


    const dataSource = [{
        createTime:'2023-09-08 12:00:00',
        coast:'10分钟',
        id:'2',
        prompterName:'dev05',
        prompterFile:'input.csv',
        promptertotalNum:'100000',
        prompterprimaryKey:'MAX(dsdsdsasdsa)',
        providerName:'dev06',
        providerFile:'output.csv',
        providertotalNum:'8000',
        providerprimaryKey:'SHA(dsdsdsasdsa)',
        intersection:'20000',
        status:'running',
    }]

    const {run:getDataList} = useRequest(getHistoryList,{
        manual:true,
        onSuccess:res=>{
            console.log('onSuccess',res);
            
        }
    })

    useMount(()=>{
        //getDataList()
    })


    const columns: ColumnsType<RowProps> = [{
        title: '创建时间/耗时',
        dataIndex: 'create',
        key: 'create',
        width:200,
        render:(text:String,row:RowProps)=>{
            const { createTime,coast} = row;
            return <><div>{createTime}</div>
                <div>{coast}</div>
            </>
        }
    },{
        title: '发起方',
        dataIndex: 'prompter',
        key: 'prompter',
        width:400,
        render:(text:String,row:RowProps)=>{
            const { prompterName,prompterFile,promptertotalNum,prompterprimaryKey} = row;
            return <><div>{prompterName}</div>
                <div>{prompterFile}</div>
                <div>数据量/主键：{promptertotalNum}/{prompterprimaryKey}</div>
            </>
        }
    },{
        title: '协作方',
        dataIndex: 'provider',
        key: 'provider',
        width:400,
        render:(text:String,row:RowProps)=>{
            const { providerName,providerFile,providertotalNum,providerprimaryKey} = row;
            return <><div>{providerName}</div>
                <div>{providerFile}</div>
                <div>数据量/主键：{providertotalNum}/{providerprimaryKey}</div>
            </>
        }
    },{
        title: '交集',
        dataIndex: 'intersection',
        key: 'intersection',
        width:80,
    },{
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width:80,
        render:(text:string)=>{
            return <Tag color="success">{text}</Tag>
        }
    }]

    const actionClickHandle = ()=>{

    }

    return  <TmTable
        dataSource={dataSource}
        columns={columns}
        rowKey="appCode"
        >
        <TmTable.Table
            actionItems={(record:RowProps) => [
            { text: '详情', key: '22'},
            { text: '删除', key: '3' },
            { text: '查看备注', key: '33' },
            ]}
        actionClickHandle={actionClickHandle}
        />
     </TmTable>
}
export default Index