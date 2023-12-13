
import { createModel } from "hox";
import { useEffect } from "react";
import { useImmer } from "use-immer";
import lodash from 'lodash'
import { useRequest } from "ahooks";
import { JOB_STATUS,JOB_PHASE_LSIT } from "@/constant/dictionary";

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
  }

  useEffect(() => {
      if(detailData.jobId){
        runGetJobDetail(detailData.jobId);
      }
  },[detailData.jobId]) 

  useEffect(() => {
    if (detailData.jobId && detailData.jobDetailData) {
      const status = lodash.get(detailData,'jobDetailData.status','');
      //首次进入页面，查询一次数据， 如果任务状态为运行中，则轮询获取多方进度
      if((status !==JOB_STATUS.EDITING && status !==JOB_STATUS.AUDITING) &&  (status === JOB_STATUS.RUNNING || (!detailData.myselfJobProgress || !detailData.partnerJobProgress))){
        runGetMergedJobProgress(detailData.jobId);
      }
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
      const myself = lodash.get(data,'myself.current_phase_progress',null);
      const myselfPhasesList = lodash.get(data,'myself.phases',[]);
      setDetailData(draft=>{
        draft.partnerJobProgress = partner;
        draft.myselfJobProgress = myself;
        draft.partnerPhasesList = partnerPhasesList;
        draft.myselfPhasesList = myselfPhasesList;
      })
      //如果任务阶段步骤完成，则取消轮询，并且重新获取任务详情
      const tmplength = JOB_PHASE_LSIT.size
      if(partnerPhasesList.length === tmplength && myselfPhasesList.length===tmplength){
        cancelGetMergedJobProgress();
        runGetJobDetail(id);
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