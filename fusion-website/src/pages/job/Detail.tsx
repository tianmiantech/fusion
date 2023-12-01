import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Col } from 'antd';
import { useImmer } from 'use-immer';
import TaskDetail from "./components/TaskDetail";
import TaskProgress from "./components/TaskProgress";
import { useParams } from "@umijs/max";
import { useRequest } from "ahooks";
import { getJobDetail } from "./service";
import Promoter from "./components/Promoter";
import { useModel } from "@umijs/max";
import lodash from 'lodash'

interface JobDetailInterface {
  role:'promoter'|'provider',
  status?:string,// 审核状态
  id?:string,
}
const Detail = () => {
  const { id } = useParams<{id: string}>();
  
  const { setJobFormData } = useModel('job.useJobForm')

  const [jobDetail, setJobDetail] = useImmer<JobDetailInterface>({
    role:'promoter'
  });

  useEffect(() => {
    if(id){
      runGetJobDetail(id);
    }
  },[id])

  const {run:runGetJobDetail} = useRequest(async (id:string)=>{
    const res = await getJobDetail(id);
    const {code,data} = res;
    if(code === 0){
      const {role,status,id,remark,myself,partner } = data
      setJobDetail(draft=>{
        draft.role = role;
      })
      let jobObj= null
      //当前角色发起方 从myself中获取数据
      if(role === 'promoter' && myself){
        jobObj = myself
      } else if(role==='provider' && partner){
        jobObj = partner
      }
      if(jobObj){
        const {bloom_filter_id,data_resource_type,hash_config,table_data_resource_info}= jobObj
        const add_method = lodash.get(table_data_resource_info,'add_method');
        setJobFormData(draft=>{
          draft.job_id = id;
          draft.initialValues = {
            remark,status,bloom_filter_id,data_resource_type,hash_config,table_data_resource_info,dataSetAddMethod:add_method}
        })
      }
     
    }
    console.log('res',res);
    
  }, {manual:true})


  

  return (
    <>
     {jobDetail.role === 'promoter'?<Promoter/>:null}
    </>
  );
};

export default Detail;
