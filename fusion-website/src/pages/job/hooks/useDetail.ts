
import { createModel } from "hox";
import { useEffect } from "react";
import { useImmer } from "use-immer";
import lodash from 'lodash'
import { useRequest } from "ahooks";
import { JOB_STATUS,JOB_PHASE_LSIT,ROLE_TYPE } from "@/constant/dictionary";

import { getJobDetail,getMergedJobProgress } from "../service";


export interface PhasesListItemInterface {
  completed_workload:number,
  cost_time:number,  //耗时  毫秒
  end_time:number,//结束时间
  job_phase:string, //所处阶段
  job_status:string,
  logs:string[],//日志
  message:string,
  percent:number, //进度百分比
  start_time:string,
  status:'doing'|'completed'|'failed', //状态
  speed_in_second:string  //每秒速度
  total_workload:number
}
interface useDetailDataInterface {
    role:'promoter'|'provider'|'',
    status?:string,// 审核状态
    jobId:string,
    jobDetailData:any,
    myselfJobCurrentProgress?:PhasesListItemInterface|null,
    partnerJobCurrentProgress?:PhasesListItemInterface|null,
    myselfPhasesList:PhasesListItemInterface[],
    partnerPhasesList:PhasesListItemInterface[],
}


const useDetail = ()=>{
    
  const [detailData, setDetailData] = useImmer<useDetailDataInterface>({
      role:'',
      jobDetailData:null,//任务详情数据
      jobId:'',
      myselfJobCurrentProgress:null, //我方当前任务进度
      partnerJobCurrentProgress:null,//协作方当前任务进度
      myselfPhasesList:[], //我方任务阶段列表
      partnerPhasesList:[],//协作方任务阶段列表

  });

  const clearDetailData = ()=>{
    setDetailData(draft=>{
      draft.role = '';
      draft.jobDetailData = null;
      draft.jobId = '';
      draft.myselfJobCurrentProgress = null;
      draft.partnerJobCurrentProgress = null;
      draft.myselfPhasesList = [];
      draft.partnerPhasesList = [];
    })
    cancelGetMergedJobProgress();
  }

  useEffect(() => {
      if(detailData.jobId){
        runGetJobDetail(detailData.jobId);
      }
  },[detailData.jobId]) 

  useEffect(() => {
    const checkResult = checkIfNeedToGetMergedJobProgress()
    if(checkResult){
      runGetMergedJobProgress(detailData.jobId);
    }
  },[detailData.jobDetailData])

  const checkIfNeedToGetMergedJobProgress = ()=>{
    const status = lodash.get(detailData,'jobDetailData.status','');
    let result = false
    if(detailData.jobId && detailData.jobDetailData){
      if(status === JOB_STATUS.RUNNING || 
        status===JOB_STATUS.WAIT_RUN ||
        (status ===JOB_STATUS.ERROR_ON_RUNNING || 
          status=== JOB_STATUS.STOP_ON_RUNNING || 
          status=== JOB_STATUS.SUCCESS && 
          (!detailData.myselfJobCurrentProgress || !detailData.partnerJobCurrentProgress))){
        result =  true
      } 
    }
    return result
  }



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
      const myself = lodash.get(data,'myself.current_phase_progress',null);
      const myselfPhasesList = lodash.get(data,'myself.phases',[]);
      const status = lodash.get(detailData,'jobDetailData.status','');
      setDetailData(draft=>{
        draft.partnerJobCurrentProgress = partner;
        draft.myselfJobCurrentProgress = myself;
        draft.partnerPhasesList = partnerPhasesList;
        draft.myselfPhasesList = myselfPhasesList;
      })
      //如果任务阶段步骤完成，则取消轮询，并且重新获取任务详情
      const tmplength = JOB_PHASE_LSIT.size
      if(partnerPhasesList.length === tmplength && myselfPhasesList.length===tmplength && status === JOB_STATUS.RUNNING){
        runGetJobDetail(id);
      } 
      const checkResult = checkIfNeedToGetMergedJobProgress()
      if(!checkResult){
        cancelGetMergedJobProgress();
      }
    }
  } ,{manual:true,pollingInterval:3000})


    return {
        detailData,
        setDetailData,
        clearDetailData
    }

}
export default createModel(useDetail);