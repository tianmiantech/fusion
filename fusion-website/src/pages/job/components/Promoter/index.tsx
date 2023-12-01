import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Col, Button, Space } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from '@umijs/max';
import JobForm from "../JobForm";
import MemberForm from "../MemberForm";
import TaskDetail from "../TaskDetail";
import RefuseModal from "../RefuseModal";
import './index.less';
import { useModel } from "@umijs/max";


interface ProvidersObj {
  isReady:boolean
}
interface StateObj {
  isReady: boolean,
  myRole: string,
  providers: ProvidersObj[]
}

interface JobFormRefInterface {
  submitFormData: () => Promise<any>
}

const Job = () => {

  const {jobFormData} = useModel('job.useJobForm')

  const promoterTitle = <strong>发起方</strong>;
  const cardStyles = {
    headStyle: {
      height: 50,
      lineHeight: 2
    },
    bodyStyle: {
      height: 'calc(100vh - 92px)',
      OverflowY: 'hidden',
      Position: 'relative'
    }
  }

  const jobFormRef = useRef<JobFormRefInterface>();


  return (
    <>
      <Row>
        <Col span={24 / ((jobFormData.job_id?1:0) + 1)}>
          <Card
            title={promoterTitle}
            size="small"
            {...cardStyles}
          >
            <JobForm ref={jobFormRef} />
          </Card>
        </Col>
        { jobFormData.job_id && <Col span={24 / ((jobFormData.job_id?1:0) + 1)}>
          <Card
            title={<strong>协作方</strong>}
            size="small"
            {...cardStyles}
          >
              <MemberForm /> 
          </Card>
        </Col>
        }
      </Row>
    </>
  );
};

export default Job;
