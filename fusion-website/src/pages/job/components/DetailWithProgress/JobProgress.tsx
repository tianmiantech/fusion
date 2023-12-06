
import { useState,useEffect } from 'react';
import type { StepProps } from 'antd';
import { Card, Steps } from 'antd';
import {JOB_PHASE_LSIT} from '@/constant/dictionary'
import lodash from 'lodash'

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
    const { data={} } = props;
    const [stepList, setStepList] = useState<StepProps[]>([]);
    const [currentStep, setCurrentStep] = useState<number>(0);

    const transJobToStepList = () => {
      const phasesList =  lodash.get(data, 'phases', []);
      setCurrentStep(phasesList.length-1)
      const tmpList = [] as StepProps[]; 
      for (const [key, description] of JOB_PHASE_LSIT) {
        const phasesIndex = lodash.findIndex(phasesList, {job_phase:key});
        const step = {
          title:description,
          status:'wait',
          description:''
        } as StepProps
        if(phasesIndex > -1){
          step.status = changeProgressStatusToStepStatus(phasesList[phasesIndex].status);
          step.description = phasesList[phasesIndex].message;
        }
        tmpList.push(step)
      }
      setStepList(tmpList);
    }

    useEffect(() => {
      if(data)
      transJobToStepList();
    } ,[data])

    const changeProgressStatusToStepStatus = (status:string) => {
      switch (status) {
        case 'doing':
          return 'process';
        case 'completed':
          return 'finish';
        case 'failed':
          return 'error';
        default:
          return 'wait'
      }
    }
    return <Steps
      current={currentStep}
      items={stepList}
    />
}
export default JobProgress;