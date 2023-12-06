
import { createModel } from "hox";
import { useEffect } from "react";
import { useImmer } from "use-immer";
import lodash from 'lodash'
import { useRequest } from "ahooks";
import { JOB_STATUS } from "@/constant/dictionary";

import { getJobDetail,getMergedJobProgress,getMyJobProgress } from "../service";

interface useDetailDataInterface {
    role:'promoter'|'provider'|'',
    status?:string,// 审核状态
    jobId?:string,
    jobDetailData:any,
    mysqlJobProgress?:any,
    partnerJobProgress?:any,
  }

const useDetail = ()=>{
    
  const [detailData, setDetailData] = useImmer<useDetailDataInterface>({
      role:'',
      jobDetailData:null,
      jobId:'',
      mysqlJobProgress:null,
      partnerJobProgress:null
  });

  const clearDetailData = ()=>{
    setDetailData(draft=>{
      draft.role = '';
      draft.jobDetailData = null;
      draft.jobId = '';
      draft.mysqlJobProgress = null;
      draft.partnerJobProgress = null;
    })
  }

  useEffect(() => {
      if(detailData.jobId){
        runGetJobDetail(detailData.jobId);
      }
  },[detailData.jobId]) 

  useEffect(() => {
    const status = lodash.get(detailData,'jobDetailData.status','');
    if(status && status !== JOB_STATUS.EDITING && status!==JOB_STATUS.AUDITING && detailData.jobId){
      runGetMergedJobProgress(detailData.jobId);
      runGetMyJobProgress(detailData.jobId)
    }
  },[detailData.jobDetailData])



  const {run:runGetJobDetail} = useRequest(async (id:string)=>{
      const res = await getJobDetail(id);
      const {code,data} = res;
      if(code === 0){
        const {role } = data
        setDetailData(draft=>{
          draft.role = role;
          draft.jobDetailData = data
        })
      }
  }, {manual:true})

  /**
   * 获取多方任务进度
   */
  const {run:runGetMergedJobProgress} = useRequest(async (id:string)=>{
    const res = await getMergedJobProgress(id);
    const {code,data} = res;
    if(code === 0){
      const partner = lodash.get(data,'partner',null);
      setDetailData(draft=>{
        draft.partnerJobProgress = partner;
      })
    }
  } ,{manual:true})

  /**
   * 主要是协作方任务进度
   */
  const {run:runGetMyJobProgress} = useRequest(async (id:string)=>{
    const res = await getMyJobProgress(id);
    const {code,data} = res;
    if(code === 0){
      setDetailData(draft=>{
        draft.mysqlJobProgress = data;
      })
    }
  } ,{manual:true})

    return {
        detailData,
        setDetailData,
        clearDetailData
    }

}
export default createModel(useDetail);