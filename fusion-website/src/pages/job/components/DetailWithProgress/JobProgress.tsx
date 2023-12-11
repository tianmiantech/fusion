
import { useState,useEffect, JSXElementConstructor, ReactElement, ReactNode, ReactPortal } from 'react';
import type { StepProps,StepsProps} from 'antd';
import { Card, Steps,Typography,Popover,Row,Col  } from 'antd';
import {JOB_PHASE_LSIT} from '@/constant/dictionary'
import lodash from 'lodash'
import useDetail from '../../hooks/useDetail';

interface JobProgressProps {
  data?:{
    phases:{
      job_phase:string, //所处阶段
      end_time:number, //结束时间
      cost_time:number, //耗时  毫秒
      logs:string[],//日志
      status:'doing'|'completed'|'failed', //状态
      message:string,
      percent:number,//进度百分比
      speed_in_second:number //每秒速度
    }[]
  }
}

const JobProgress = (props:JobProgressProps) => {
    const {detailData} = useDetail();
    const [stepList, setStepList] = useState<StepProps[]>([]);
    const [currentStep, setCurrentStep] = useState<number>(0);


    useEffect(()=>{
     
      const tmpList = [] as StepProps[]; 
      const myselfPhasesListLenght = detailData.myselfPhasesList.length;
      const partnerPhasesListLenght = detailData.partnerPhasesList.length;
      //设置当前步骤
      setCurrentStep(Math.max(0, Math.min(myselfPhasesListLenght-1, partnerPhasesListLenght-1)))
      for (const [key, description] of JOB_PHASE_LSIT) {
        const step = {
          title:description,
          status:'wait',
          description:''
        } as StepProps
        if(myselfPhasesListLenght>0 && partnerPhasesListLenght>0){
          const myselfPhasesIndex = lodash.findIndex(detailData.myselfPhasesList, {job_phase:key});
          const partnerPhasesIndex = lodash.findIndex(detailData.partnerPhasesList, {job_phase:key});
          
          if(myselfPhasesIndex > -1 && partnerPhasesIndex>-1){
            step.status = changeProgressStatusToStepStatus(detailData.myselfPhasesList[myselfPhasesIndex].status,detailData.partnerPhasesList[partnerPhasesIndex].status);
            step.description = ''
          }
        }
        tmpList.push(step)
      }
      setStepList(tmpList);
    },[detailData.myselfPhasesList?.length,detailData.partnerPhasesList?.length])

    /**
     * 将双发进度状态合并，有一方失败则整体失败，有正在进行则进行中，都完成才展示完成
     * @param myselfStatus 
     * @param partnerStatus 
     * @returns 
     */
    const changeProgressStatusToStepStatus = (myselfStatus:string,partnerStatus:string) => {
      if(myselfStatus === 'failed' || partnerStatus === 'failed'){
        return 'error';
      } else if(myselfStatus === 'doing' || partnerStatus === 'doing'){
        return 'process';
      } else if(myselfStatus === 'completed' && partnerStatus === 'completed') {
        return 'finish';
      } else 
        return 'wait'
    }



    const renderCustomDotContent = (index:number)=>{
      const myselfPhasesListLenght = detailData.myselfPhasesList.length;
      const partnerPhasesListLenght = detailData.partnerPhasesList.length||0;
      let myselfMsg = '',partnerMsg = '';
      if(myselfPhasesListLenght>0 && myselfPhasesListLenght-1>=index){
        myselfMsg = detailData.myselfPhasesList[index].message;
      }
      if(partnerPhasesListLenght>0 && partnerPhasesListLenght-1>=index){
        partnerMsg = detailData.partnerPhasesList[index].message;
      }
      if(myselfMsg || partnerMsg){
        return <>
        {myselfMsg?<span><strong>【我方】</strong>{myselfMsg}</span> : ''}
        {myselfMsg?<br/>:''}
        {partnerMsg?<span><strong>【协作方】</strong>{partnerMsg}</span> : ''}
        </>
      }
      return null
     
    }
    const customDot: StepsProps['progressDot'] = (dot: any, { status, index }: any) => {

      return  <Popover
      content={renderCustomDotContent(index)
      }
    >
      {dot}
    </Popover>
    }

    return <Steps
      progressDot={customDot}
      current={currentStep}
      items={stepList}
    />
}
export default JobProgress;