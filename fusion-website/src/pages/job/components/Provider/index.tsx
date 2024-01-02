import { useEffect, useState, useRef, useMemo,useImperativeHandle,forwardRef } from "react";
import { Space, Row, Col, Button, message, Spin } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from '@umijs/max';
import JobForm from "../JobForm";
import SendJobForm from "../SendJobForm";
import ReadOnlyDetailItem from "../ReadOnlyDetailItem";
import RefuseModal from "../RefuseModal";
import './index.less';
import lodash from 'lodash'
import JobCard from '../JobCard'
import useDetail from "../../hooks/useDetail";
import { useRequest } from 'ahooks';
import {agreeAndStart,CreateJobRequestInterface,DisagreeJobRequestInterface,disagreeJob} from '../../service'

interface PromoterPropsInterface {
  detailData?:any
}
/**
 * 发起方Job页面
 */
const Index = forwardRef((props:PromoterPropsInterface,ref) => {
 
  const { detailData,clearDetailData } = useDetail()
  const jobFormRef = useRef<any>();

  const refuseModalRef = useRef<any>();
 
  useEffect(()=>{
    if(detailData.jobDetailData){
      jobFormRef.current.setFieldsValue({
        algorithm:lodash.get(detailData,'jobDetailData.algorithm')
      })
    }
  },[detailData.jobDetailData])

  const {run:runAgreeAndStart,loading:agreeAndStartLoading} = useRequest(async (params:CreateJobRequestInterface)=>{
    const reponse = await agreeAndStart(params)
    const {code,data} = reponse;
    if(code === 0){
      message.success('操作成功')
      clearDetailData()
      setTimeout(()=>{
        history.push('/job/list')
      },800)
    }  
  },{ manual:true})



  const submitFormData = async () => {
    const {data_resource_type,add_method,hash_config,remark,algorithm,table_data_resource_info,additional_result_columns,bloom_filter_resource_input=null} = await jobFormRef.current?.validateFields();
    const requestParams = {
      remark,
      algorithm,
      job_id:detailData.jobId,
      data_resource:{
        data_resource_type,
        table_data_resource_info,
        hash_config,
        add_method,
        additional_result_columns,
        bloom_filter_resource_input
      }
    }
    runAgreeAndStart(requestParams)
  }

  

  const renderFormAction = ()=>{
    return  <Spin spinning={agreeAndStartLoading}>
    <Space size={30}>
      <Button
        type="primary"
        danger
        onClick={()=>{refuseModalRef.current.showRefuseModal()}}
      >拒绝</Button>
      <Button
        type="primary"
        onClick={() => submitFormData()}
      >通过并开启任务</Button>
    </Space>
  </Spin>

   }


  return (
    <>
      <Row>
        <Col span={12}>
           <ReadOnlyDetailItem title={'发起方'}  bodyStyle={{ height: 'calc(100vh - 92px)'}} detailInfoData={detailData.jobDetailData?.partner}/>
        </Col>
        <Col span={12}>
          <JobCard
            title={'协作方'}
            bodyStyle={{ height: 'calc(100vh - 92px)'}}
          >
             <JobForm
               ref={jobFormRef}
               loading={agreeAndStartLoading}
               renderFormAction={renderFormAction}
             />
          </JobCard>
        </Col>
      </Row>
      <RefuseModal
        ref={refuseModalRef}
      />
    </>
  );
});

export default Index;
