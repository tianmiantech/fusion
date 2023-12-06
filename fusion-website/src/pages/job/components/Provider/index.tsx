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
 
  const { detailData,setDetailData } = useDetail()
  const jobFormRef = useRef<any>();
  const [showRefuseModal,setShowRefuseModal] = useState(false)

  const [promoterDetail,setPromoterDetail] = useState()

  const {run:runAgreeAndStart,loading:agreeAndStartLoading} = useRequest(async (params:CreateJobRequestInterface)=>{
    const reponse = await agreeAndStart(params)
    const {code,data} = reponse;
    if(code === 0){
      message.success('操作成功')
      setDetailData(g=>{
        g.jobId = lodash.get(data,'job_id');
      })
    }  
  },{ manual:true})

const {run:runDisagreeJob,loading:disagreeJobLoading} = useRequest(async (params:DisagreeJobRequestInterface)=>{
  const reponse = await disagreeJob(params)
  const {code,data} = reponse;
  if(code === 0){
    message.success('操作成功')
  }}
  ,{ manual:true})

  const submitFormData = async () => {
    const {data_resource_type,dataSetAddMethod,hash_config,remark,table_data_resource_info} = await jobFormRef.current?.validateFields();
    const requestParams = {
      remark,
      job_id:detailData.jobId,
      data_resource:{
        data_resource_type,
        table_data_resource_info,
        hash_config
      }
    }
    runAgreeAndStart(requestParams)
  }

  const submitDisagreeJob = (value:string)=>{
    const requestParams = {
      job_id:detailData.jobId,
      reason:value
    } as DisagreeJobRequestInterface
    runDisagreeJob(requestParams)
  }

  const renderFormAction = ()=>{
    return  <Spin spinning={agreeAndStartLoading|| disagreeJobLoading}>
    <Space size={30}>
      <Button
        type="primary"
        danger

      >拒绝</Button>
      <Button
        type="primary"
        onClick={() => submitFormData()}
      >通过</Button>
    </Space>
  </Spin>

   }


  return (
    <>
      <Row>
        <Col span={12}>
          <JobCard
            title={'发起方'}
            bodyStyle={{ height: 'calc(100vh - 92px)'}}
          >
           <ReadOnlyDetailItem detailInfoData={detailData.jobDetailData?.partner}/>
          </JobCard>
        </Col>
        <Col span={12}>
          <JobCard
            title={'协作方'}
            bodyStyle={{ height: 'calc(100vh - 92px)'}}
          >
             <JobForm
               ref={jobFormRef}
               loading={agreeAndStartLoading|| disagreeJobLoading}
               renderFormAction={renderFormAction}
             />
          </JobCard>
        </Col>
      </Row>
      <RefuseModal
        open={showRefuseModal}
        onCancel={()=>{setShowRefuseModal(false)}}
        onOk={submitDisagreeJob}
      />
    </>
  );
});

export default Index;
