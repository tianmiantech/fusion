import { useEffect, useState, useRef, useMemo,useImperativeHandle,forwardRef } from "react";
import { Card, Row, Col, Button, Space } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from '@umijs/max';
import JobForm from "../JobForm";
import SendJobForm from "../SendJobForm";
import TaskDetail from "./PrompoterDetail";
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
 


  return (
    <>
      <Row>
        <Col span={12}>
          <JobCard
            title={'发起方'}
          >
           <TaskDetail />
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
