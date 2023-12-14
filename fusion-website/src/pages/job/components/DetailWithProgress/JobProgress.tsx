
import { useState,useEffect, JSXElementConstructor, ReactElement, ReactNode, ReactPortal } from 'react';
import type { StepProps,StepsProps} from 'antd';
import { Card, Steps,Typography,Popover,Row,Col,Progress,List } from 'antd';
import { ProDescriptions } from '@ant-design/pro-components';
import {JOB_PHASE_LSIT} from '@/constant/dictionary'
import lodash from 'lodash'
import useDetail from '../../hooks/useDetail';
import type {PhasesListItemInterface} from '../../hooks/useDetail';
import {displayChineseCoastTime} from '@/utils/time'
import styles from './index.less'


interface JobProgressProps {
  promoterPhasesList:PhasesListItemInterface[],
  providerPhasesList:PhasesListItemInterface[],

}


const JobProgress = (props:JobProgressProps) => {
    const {promoterPhasesList,providerPhasesList} = props
    const [stepList, setStepList] = useState<StepProps[]>([]);
    const [currentStep, setCurrentStep] = useState<number>(0);


    useEffect(()=>{
      const tmpList = [] as StepProps[]; 
      const promoterPhasesListLenght = promoterPhasesList.length;
      const providerPhasesListLenght = providerPhasesList.length;
      //设置当前步骤
      setCurrentStep(Math.max(0, Math.min(promoterPhasesListLenght-1, providerPhasesListLenght-1)))
      for (const [key, description] of JOB_PHASE_LSIT) {
        const step = {
          title:description,
          status:'wait',
          description:'未执行到此处'
        } as StepProps
        const promoterPhasesObj = lodash.find(promoterPhasesList, {job_phase:key},null);
        const providerPhases = lodash.find(providerPhasesList, {job_phase:key},null);
        if(promoterPhasesObj || providerPhases ){
          const myselfStatus = promoterPhasesObj?.status;
          const partnerStatus = providerPhases?.status;
          step.status = changeProgressStatusToStepStatus(myselfStatus,partnerStatus);
          step.description = renderDescription(promoterPhasesObj,promoterPhasesObj)
        }
        tmpList.push(step)
      }
      setStepList(tmpList);
    },[promoterPhasesList.length,providerPhasesList.length])

    const renderDescription = (promoterPhasesObj:PhasesListItemInterface,providerPhases:PhasesListItemInterface)=>{
      return <>
      <Row >

          <Col span={12}>
            <Card>
              {renderPhasesItem(promoterPhasesObj,'发起方')}
            </Card>
            
          </Col>
          <Col span={12} >
            <Card>
              {renderPhasesItem(providerPhases,'协作方')}
            </Card>
          </Col>
      </Row>
      </>
    }



    const renderPhasesItem = (phasesObj:PhasesListItemInterface,title:string)=>{
      return <>
      <ProDescriptions column={1} title={renderPhasesItemTitle(title,phasesObj)} labelStyle={{textAlign:'right'}}>
        <ProDescriptions.Item label='任务进度'>
          <Progress style={{width:'70%'}} percent={lodash.get(phasesObj,'percent',0)} />
        </ProDescriptions.Item>
        <ProDescriptions.Item label='耗时'>
          {displayChineseCoastTime(lodash.get(phasesObj,'cost_time',0))}
        </ProDescriptions.Item> 
        <ProDescriptions.Item label='日志' >
          {renderLogs(lodash.get(phasesObj,'logs',[]))}
        </ProDescriptions.Item> 
      </ProDescriptions>
      </>
    }

    const renderPhasesItemTitle = (title:string,phasesObj:PhasesListItemInterface)=>{
      const msg = lodash.get(phasesObj,'message','');
      const status = lodash.get(phasesObj,'status','');
      let color = 'gray';
      if(status === 'failed'){
        color = 'red';
      } 
      if(!msg)
        return <span>{title}</span>
      return <span>{title}<span style={{ fontSize:12,color:color}}>（{msg}）</span></span>
    }

    const renderLogs = (logs:string[])=>{
      return <ul className={styles.logContainer}>
          {logs.map((log, index) => (
                <li key={index}>{log}</li>
              ))}
      </ul>
    }
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
      direction="vertical"
      current={currentStep}
      items={stepList}
    />
}
export default JobProgress;