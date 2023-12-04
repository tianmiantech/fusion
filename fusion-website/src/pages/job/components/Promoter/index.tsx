import { useEffect, useState, useRef, useMemo,useImperativeHandle,forwardRef } from "react";
import { Card, Row, Col, Button, Space } from 'antd';
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
interface PromoterPropsInterface {
  detailData?:any
}
/**
 * 发起方Job页面
 */
const Index = forwardRef((props:PromoterPropsInterface,ref) => {

  const { detailData } = useDetail()

  const renderCardTitlte = () => {
    if (detailData.jobId && detailData.jobDetailData?.status ==='editing') {
      return <>发起方<span style={{fontSize:12,color:'gray'}}>（已保存，待发起任务,发起任务后 数据将不可更改）</span></>
    }
    return <>发起方</>
  }

  return (
    <>
      <Row>
        <Col span={24 / ((detailData.jobId?1:0) + 1)}>
          <JobCard
            title={renderCardTitlte()}
          >
            <JobForm/>
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
