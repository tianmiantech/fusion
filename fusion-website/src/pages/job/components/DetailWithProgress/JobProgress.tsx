
import { useState,useEffect, JSXElementConstructor, ReactElement, ReactNode, ReactPortal } from 'react';
import type { StepProps,StepsProps} from 'antd';
import { Card, Steps,Typography,Popover,Row,Col,Progress,Collapse } from 'antd';
import { LoadingOutlined, SmileOutlined, CheckCircleOutlined, UserOutlined,CloseCircleOutlined } from '@ant-design/icons';
import { ProDescriptions } from '@ant-design/pro-components';
import lodash from 'lodash'
import type {PhasesListItemInterface,PhasesStpesListItemInterface} from '../../hooks/useDetail';
import {displayChineseCoastTime} from '@/utils/time'
import styles from './index.less'
import useDetail from '../../hooks/useDetail';
import { useImmer } from 'use-immer';

interface JobProgressProps {
  promoterPhasesList:PhasesListItemInterface[],
  providerPhasesList:PhasesListItemInterface[],

}

const { Panel } = Collapse;
const JobProgress = (props:JobProgressProps) => {
    const {detailData} = useDetail();

    const {promoterPhasesList,providerPhasesList} = props
    const [stepList, setStepList] = useState<StepProps[]>([]);
    //记录打开的折叠面板
    const [currentStepOpenKey, setCurrentStepOpenKey] = useState<string[]>([]);


    useEffect(()=>{
      //设置步骤 步骤数据源来自于接口
      if(detailData.phasesStpesList.length>0){
        const tmpList = [] as StepProps[]; 
        detailData.phasesStpesList.map((item:PhasesStpesListItemInterface,index:number)=>{
          const {name,phase} = item;
          const step = {
            title:name,
            status:'wait',
            description:'未执行到此处',
            icon:<SmileOutlined />
          } as StepProps
          const promoterPhasesObj = lodash.find(promoterPhasesList, {job_phase:phase},null);
          const providerPhases = lodash.find(providerPhasesList, {job_phase:phase},null);
          if(promoterPhasesObj || providerPhases ){
            const myselfStatus = promoterPhasesObj?.status;
            const partnerStatus = providerPhases?.status;
            console.log('myselfStatus',myselfStatus);
            console.log('partnerStatus',partnerStatus);
            
            const status = changeProgressStatusToStepStatus(myselfStatus,partnerStatus);
            step.status = status;
            step.icon = getIconByStatus(status);
            step.description = renderDescription(promoterPhasesObj,providerPhases,phase)
            // 展示正在进行的步骤
            // if(status === 'process'){
            //   const tmpCurrentStepOpenKey = JSON.parse(JSON.stringify(currentStepOpenKey))
            //   if(!tmpCurrentStepOpenKey.includes(phase)){
            //     tmpCurrentStepOpenKey.push(phase);
            //   }
            //   setCurrentStepOpenKey(tmpCurrentStepOpenKey)
            // }
          }
          tmpList.push(step)
        })
        setStepList(tmpList);
      }
    },[promoterPhasesList,providerPhasesList,detailData.phasesStpesList.length,currentStepOpenKey.length])

    const onCollapseChange = (key:string)=>{
      const tmpCurrentStepOpenKey = JSON.parse(JSON.stringify(currentStepOpenKey))
      const index =lodash.findIndex(tmpCurrentStepOpenKey, (item:string)=>item ===key );
      if(index>-1){
        tmpCurrentStepOpenKey.splice(index,1);
      }else{
        tmpCurrentStepOpenKey.push(key);
      }
      setCurrentStepOpenKey(tmpCurrentStepOpenKey)
    }

    const renderDescription = (promoterPhasesObj:PhasesListItemInterface,providerPhases:PhasesListItemInterface,phase:string)=>{
      return <Collapse activeKey={currentStepOpenKey.includes(phase)?phase:''}  onChange={()=>onCollapseChange(phase)}>
       <Panel header="进度详情" key={phase}>
        <Row >
          <Col span={12} >
            <Card>
              {renderPhasesItem(promoterPhasesObj,'发起方')}
            </Card>
          </Col>
          <Col span={12} >
            {providerPhases && <Card>
              {renderPhasesItem(providerPhases,'协作方')}
            </Card>}
            
          </Col>
        </Row>
      </Panel>
      </Collapse>
    }



    const renderPhasesItem = (phasesObj:PhasesListItemInterface,title:string)=>{
      if(!phasesObj)
        return <></>
      const {skip_this_phase} = phasesObj;
      return <>
      <ProDescriptions column={1} title={renderPhasesItemTitle(title,phasesObj)} labelStyle={{textAlign:'right'}}>
        {
          !skip_this_phase && <>
            <ProDescriptions.Item label='任务进度'>
             <Progress style={{width:'70%'}} percent={lodash.get(phasesObj,'percent',0)} />
            </ProDescriptions.Item>
            <ProDescriptions.Item label='耗时'>
              {displayChineseCoastTime(lodash.get(phasesObj,'cost_time',0))}
            </ProDescriptions.Item> 
          </>
        }
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
      console.log(myselfStatus,partnerStatus);   
      if(myselfStatus === 'failed' || partnerStatus === 'failed'){
        return 'error';
      } else if(myselfStatus === 'running' || partnerStatus === 'running' ){
        return 'process';
      } else if(myselfStatus === 'completed' && partnerStatus === 'completed') {
        return 'finish';
      } else if (myselfStatus === 'completed' || partnerStatus === 'completed') { //有一方完成，另一方未完成表示在执行中
        return 'process';
      }
        return 'wait'
    }

    const getIconByStatus = (status:string)=>{
      if(status === 'error'){
        return <CloseCircleOutlined />
      }else if(status === 'process'){
        return <LoadingOutlined />
      }else if(status === 'finish'){
        return <CheckCircleOutlined />
      } 
      return <SmileOutlined />
    }

    console.log('stepList',stepList);
    
    
    return <Steps
      direction="vertical"
      items={stepList}
    />
}
export default JobProgress;