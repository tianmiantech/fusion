
import { createModel } from "hox";
import { useEffect } from "react";
import { useImmer } from "use-immer";
import lodash from 'lodash'
import { useRequest } from "ahooks";
import { JOB_STATUS } from "@/constant/dictionary";

import { getJobDetail,getMergedJobProgress,getAlgorithmPhaseList } from "../service";


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
  total_workload:number,
  skip_this_phase:boolean,//是否跳过
}

export interface PhasesStpesListItemInterface {
  name:string,
  phase:string

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
    phasesStpesList:PhasesStpesListItemInterface[] //任务详情阶段
    lastJobStatus:string //上一次任务状态
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
      phasesStpesList:[],
      lastJobStatus:''
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
      draft.lastJobStatus='';
    })
    cancelGetMergedJobProgress();
  }

  useEffect(() => {
      if(detailData.jobId){
        runGetJobDetail(detailData.jobId);
      }
  },[detailData.jobId]) 

  useEffect(() => {
    const job_status = lodash.get(detailData,'jobDetailData.status','')
    if(job_status){
      const exceptArray = [JOB_STATUS.AUDITING,JOB_STATUS.EDITING,JOB_STATUS.DISAGREE]
      if(!exceptArray.includes(job_status)){
        runGetMergedJobProgress(detailData.jobId);
      }
    }
  },[detailData.jobDetailData?.status])

  const checkIfNeedToGetMergedJobProgress = ()=>{
    const job_status = lodash.get(detailData,'jobDetailData.status','')
    if(job_status === JOB_STATUS.RUNNING || job_status===JOB_STATUS.WAIT_RUN ){
      return true
    }
    return false
  }



  const {run:runGetJobDetail} = useRequest(async (id:string)=>{
      const res = await getJobDetail(id);
      const {code,data} = res;
      if(code === 0){
        const {role,algorithm,status } = data

        setDetailData(draft=>{
          draft.role = role;
          draft.jobDetailData = data
        })
        if(detailData.phasesStpesList.length == 0){
          runGetAlgorithmPhaseList(algorithm)
        }
      }
  }, {manual:true})

  //不同的算法有不同的阶段步骤
  const {run:runGetAlgorithmPhaseList} = useRequest(async (algorithm:string)=>{
    const res = await getAlgorithmPhaseList(algorithm);
    const {code,data} = res;
    if(code === 0){
      setDetailData(draft=>{
        draft.phasesStpesList = lodash.get(data,'list',[])
      })
    }
  },{manual:true})

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
      const myJobStatus = lodash.get(data,'myself.job_status','');
      const lastJobStatus = lodash.get(detailData,'lastJobStatus','');
      console.log('myJobStatus',myJobStatus);
      console.log('lastJobStatus',lastJobStatus);
      
      setDetailData(draft=>{
        draft.partnerJobCurrentProgress = partner;
        draft.myselfJobCurrentProgress = myself;
        draft.partnerPhasesList = partnerPhasesList;
        draft.myselfPhasesList = myselfPhasesList;
        draft.lastJobStatus = myJobStatus;
      })
      //如果任务阶段步骤完成，则取消轮询，并且重新获取任务详情
      if(lastJobStatus && myJobStatus !== lastJobStatus){
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