
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
    jobId:string,
    jobDetailData:any,
    myselfJobProgress?:any,
    partnerJobProgress?:any,
    myselfPhasesList:any[],
    partnerPhasesList:any[],
  }

const useDetail = ()=>{
    
  const [detailData, setDetailData] = useImmer<useDetailDataInterface>({
      role:'',
      jobDetailData:null,//任务详情数据
      jobId:'',
      myselfJobProgress:null, //我方当前任务进度
      partnerJobProgress:null,//协作方当前任务进度
      myselfPhasesList:[], //我方任务阶段列表
      partnerPhasesList:[],//协作方任务阶段列表
  });

  const clearDetailData = ()=>{
    setDetailData(draft=>{
      draft.role = '';
      draft.jobDetailData = null;
      draft.jobId = '';
      draft.myselfJobProgress = null;
      draft.partnerJobProgress = null;
      draft.myselfPhasesList = [];
      draft.partnerPhasesList = [];
    })
    cancelGetMergedJobProgress();
    cancelGetMyJobProgress();
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
  const {run:runGetMergedJobProgress,cancel:cancelGetMergedJobProgress} = useRequest(async (id:string)=>{
    const res = await getMergedJobProgress(id);
    const {code,data} = res;
    if(code === 0){
      const partner = lodash.get(data,'partner.current_phase_progress',null);
      const partnerPhasesList = lodash.get(data,'partner.phases',[]);
      setDetailData(draft=>{
        draft.partnerJobProgress = partner;
        draft.partnerPhasesList = partnerPhasesList;
      })
    }
  } ,{manual:true,pollingInterval:3000})

  /**
   * 主要是协作方任务进度
   */
  const {run:runGetMyJobProgress,cancel:cancelGetMyJobProgress} = useRequest(async (id:string)=>{
    const res = await getMyJobProgress(id);
    const {code,data} = res;
    if(code === 0){
      const myselfJobProgress = lodash.get(data,'current_phase_progress',null);
      const myselfPhasesList = lodash.get(data,'phases',[]);
      setDetailData(draft=>{
        draft.myselfJobProgress = myselfJobProgress;
        draft.myselfPhasesList = myselfPhasesList;
      })
    }
  } ,{manual:true,pollingInterval:3000})

    return {
        detailData,
        setDetailData,
        clearDetailData
    }

}
export default createModel(useDetail);