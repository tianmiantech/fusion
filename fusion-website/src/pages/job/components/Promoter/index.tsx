import { useEffect, useState, useRef, useMemo,useImperativeHandle,forwardRef } from "react";
import { Card, Row, Col, Button,message } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from '@umijs/max';
import JobForm from "../JobForm";
import SendJobForm from "../SendJobForm";
import TaskDetail from "../Provider/PrompoterDetail";
import RefuseModal from "../RefuseModal";
import lodash from 'lodash'
import JobCard from '../JobCard'
import useDetail from "../../hooks/useDetail";
import { useRequest } from 'ahooks';
import {createJob,CreateJobRequestInterface} from '../../service'
interface PromoterPropsInterface {
  detailData?:any
}
/**
 * 发起方Job页面
 */
const Index = forwardRef((props:PromoterPropsInterface,ref) => {

  const { detailData,setDetailData } = useDetail()

  const jobFormRef = useRef<any>();

  const renderCardTitlte = () => {
    if (detailData.jobId && detailData.jobDetailData?.status ==='editing') {
      return <>发起方<span style={{fontSize:12,color:'gray'}}>（已保存，待发起任务,发起任务后 数据将不可更改）</span></>
    }
    return <>发起方</>
  }

  const {run:runCreateJob,loading:createJobloading} = useRequest(async (params:CreateJobRequestInterface)=>{
    const reponse = await createJob(params)
    const {code,data} = reponse;
    if(code === 0){
      message.success('保存成功')
      setDetailData(g=>{
        g.jobId = lodash.get(data,'job_id');
      })
    }  
  },{ manual:true})

  const submitFormData = async () => {
    const {data_resource_type,dataSetAddMethod,hash_config,remark,table_data_resource_info} = await jobFormRef.current?.validateFields();
    const requestParams = {
      remark,
      data_resource:{
        data_resource_type,
        table_data_resource_info,
        hash_config
      }
    }
    runCreateJob(requestParams)
  }





  const renderFormAction = ()=>{
   return  <Button loading={createJobloading} disabled={detailData.jobDetailData && lodash.get(detailData,'jobDetailData.status','')!=='editing'} type="primary" onClick={submitFormData}>{detailData.jobId?'更新':'保存'}</Button>
  }

  return (
    <>
      <Row>
        <Col span={24 / ((detailData.jobId?1:0) + 1)}>
          <JobCard
            title={renderCardTitlte()}
          >
            <JobForm 
              ref={jobFormRef}
              loading={createJobloading}
              renderFormAction={renderFormAction}
            />
          </JobCard>
        </Col>
        { detailData.jobId && <Col span={24 / ((detailData.jobId?1:0) + 1)}>
          <JobCard
            title={'协作方'}
          >
              <SendJobForm /> 
          </JobCard>
        </Col>
        }
      </Row>
    </>
  );
});

export default Index;
