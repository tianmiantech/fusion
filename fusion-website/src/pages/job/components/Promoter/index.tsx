import { useEffect, useState, useRef, useMemo,useImperativeHandle,forwardRef } from "react";
import { Card, Row, Col, Button,message } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from '@umijs/max';
import JobForm from "../JobForm";
import SendJobForm from "../SendJobForm";
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
    const {data_resource_type,hash_config,remark,table_data_resource_info=null,bloom_filter_resource_input=null,algorithm,additional_result_columns} = await jobFormRef.current?.validateFields();
    const requestParams = {
      remark,
      algorithm,
      data_resource:{
        data_resource_type,
        hash_config,
        table_data_resource_info,
        bloom_filter_resource_input,
        additional_result_columns
      }
    }
    runCreateJob(requestParams)
  }


  const renderFormAction = ()=>{
    const status = lodash.get(detailData,'jobDetailData.status','')
    return  <Button disabled={!(!status || status==='editing')} loading={createJobloading} type="primary" onClick={submitFormData}>{detailData.jobId?'更新':'保存'}</Button>
  }

  const renderProviderTitle = ()=>{
    const status = lodash.get(detailData,'jobDetailData.status','')
    const member_id = lodash.get(detailData,'jobDetailData.partner.member_id','')
    if(!status || status==='editing'){
      return '协作方'
    } else {
      return `协作方（${member_id}）`
    }
  }

   /**
   *  初始化表格填充内容
    remark:string,
    status:string,
    data_resource_type:string,
    hash_config:{},
    table_data_resource_info:{},
    bloom_filter_id:string,
   */
    useEffect(()=>{
      if(detailData.jobDetailData){
        jobFormRef.current.setFieldsValue({
          remark:lodash.get(detailData,'jobDetailData.remark',''),
          status: lodash.get(detailData,'jobDetailData.status','') ,
          data_resource_type: lodash.get(detailData,'jobDetailData.myself.data_resource_type',''),
          hash_config: {...lodash.get(detailData,'jobDetailData.myself.hash_config',''),source:'setFieldsValue'},
          table_data_resource_info:{...lodash.get(detailData,'jobDetailData.myself.table_data_resource_info',{}),source:'setFieldsValue'},
          bloom_filter_resource_input:{bloom_filter_id:lodash.get(detailData,'jobDetailData.myself.bloom_filter_id',''),source:'setFieldsValue'},
          add_method:lodash.get(detailData,'jobDetailData.myself.table_data_resource_info.add_method'),
          algorithm:lodash.get(detailData,'jobDetailData.algorithm'),
          additional_result_columns:lodash.get(detailData,'jobDetailData.myself.additional_result_columns',[]),
        })
      }
    },[detailData.jobDetailData])


  

  return (
    <>
      <Row>
        <Col span={24 / ((detailData.jobId?1:0) + 1)}>
          <JobCard
            title={renderCardTitlte()}
            bodyStyle={{ height: 'calc(100vh - 92px)',}}
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
            title={renderProviderTitle()}
            bodyStyle={{ height: 'calc(100vh - 92px)'}}
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
