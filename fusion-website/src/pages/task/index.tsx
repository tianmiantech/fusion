import { useEffect, useState, useRef } from "react";
import { Card, Row, Col, Button, Space } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import TaskForm from "./components/TaskForm";
import ProviderForm from "./components/ProviderForm";
import TaskDetail from "./components/TaskDetail";
import RefuseModal from "./components/RefuseModal";
import './index.less';
import { TmLayout, TmInput } from '@tianmiantech/pro';

const Task = () => {
  const promoterTitle = <strong>发起方 10.10.105.22:90</strong>;
  const cardStyles = {
    headStyle: {
      height: 50,
      lineHeight: 2
    },
    bodyStyle: {
      height: 'calc(100vh - 92px)',
      overflowY: 'hidden',
      position: 'relative'
    }
  }

  const taskFormRef = useRef();
  const [state, setState] = useImmer({
    isReady: true,
    myRole: 'provider'
  });

  const [refuseOpen, setRefuseOpen] = useState(false);

  const savePromoterForm = () => {
    setState((g) => {
      g.isReady = true;
    });
  }

  return (
    <Row>
      <Col span={state.isReady ? 12 : 24}>
        <Card
          title={promoterTitle}
          size="small"
          {...cardStyles}
        >
          {
            state.myRole === 'promoter' ?
            <TaskForm ref={taskFormRef} /> :
            <TaskDetail />
          }
          <Row className="operation-area">
            {
              state.isReady ?
              <>
                <CheckCircleFilled style={{ fontSize: 24, color: '#52c41a' }} />
                <span>已保存，待发起任务</span>
              </> :
              <Button type="primary" onClick={savePromoterForm}>保存</Button>
            }
          </Row>
        </Card>
      </Col>
      {
        state.isReady ?
        <Col span={12}>
          <Card
            title={<strong>协作方</strong>}
            extra={
              state.myRole === 'promoter' ?
              <Button>添加协作方</Button> : null
            }
            size="small"
            {...cardStyles}
          >
            {
              state.myRole === 'promoter' ?
              <ProviderForm /> :
              <TaskForm ref={taskFormRef} />
            }
            <Row className="operation-area">
              {
                state.myRole === 'promoter' ?
                <Button type="primary">发起任务</Button> :
                <>
                  <Space size={30}>
                    <Button
                      type="primary"
                      danger
                      onClick={() => setRefuseOpen(true)}
                    >拒绝</Button>
                    <Button type="primary">通过</Button>
                  </Space>
                  <RefuseModal
                    open={refuseOpen}
                    onCancel={() => setRefuseOpen(false)}
                  />
                </>
              }
            </Row>
          </Card>
        </Col> : null
      }
    </Row>
  );
};

export default Task;
