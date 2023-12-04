import { useEffect, useState, useRef, useMemo,useImperativeHandle,forwardRef } from "react";
import { Card, Row, Col, Button, Space } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from '@umijs/max';
import JobForm from "../JobForm";
import SendJobForm from "../SendJobForm";
import TaskDetail from "../TaskDetail";
import RefuseModal from "../RefuseModal";
import './index.less';
import lodash from 'lodash'
import JobCard from '../JobCard'
interface PromoterPropsInterface {
  detailData?:any
}
/**
 * 发起方Job页面
 */
const Index = forwardRef((props:PromoterPropsInterface,ref) => {
 
  const {detailData={}} = props

  const jobRef = useRef<any>();

  useEffect(()=>{
    if(detailData){
      const {role,status,id,remark,myself={},partner } = detailData
      const {bloom_filter_id,data_resource_type,hash_config,table_data_resource_info}= myself
      const add_method = lodash.get(table_data_resource_info,'add_method');
      jobRef.current?.setJobFormData((draft:any)=>{
        draft.job_id = id;
        draft.role = role
        draft.initialValues = {
          remark,status,bloom_filter_id,data_resource_type,hash_config,table_data_resource_info,dataSetAddMethod:add_method}
      })
    }
  },[
    detailData
  ])

  return (
    <>
      <Row>
        <Col span={12}>
          <JobCard
            title={'发起方'}
          >
            <JobForm ref={jobRef}/>
          </JobCard>
        </Col>
        <Col span={12}>
          <JobCard
            title={'协作方'}
          >
             <JobForm/>
          </JobCard>
        </Col>
      </Row>
    </>
  );
});

export default Index;
