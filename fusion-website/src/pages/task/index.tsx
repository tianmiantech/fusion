import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Col, Button, Space } from 'antd';
import { CheckCircleFilled } from '@ant-design/icons';
import { useImmer } from 'use-immer';
import { history } from 'umi';
import TaskForm from "./components/TaskForm";
import ProviderForm from "./components/ProviderForm";
import TaskDetail from "./components/TaskDetail";
import RefuseModal from "./components/RefuseModal";
import './index.less';

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
    isReady: false,
    myRole: 'promoter',
    providers: []
  });

  const [refuseOpen, setRefuseOpen] = useState(false);

  const savePromoterForm = () => {
    setState((g) => {
      g.isReady = true;
      if (g.providers.length === 0) {
        g.providers.push({
          isReady: false
        })
      }
    });
  }

  const addProvider = () => {
    setState((g) => {
      g.providers.push({
        isReady: false
      });
    });
  }

  const providerPass = (index:number) => {
    setState((g) => {
      g.providers[index].isReady = true;
    });
    localStorage.setItem('hasTask', true);
    history.push('/task/detail');
  }

  const switchProvider = () => {
    setState((g) => {
      g.myRole = 'provider';
    });
  }

  return (
    <>
      <Row>
        <Col span={24 / (state.providers?.length + 1)}>
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
          (state.isReady && state.providers.length) ?
          state.providers.map((provider, index) => {
            return (
              <Col key={index} span={24 / (state.providers.length + 1)}>
                <Card
                  title={<strong>协作方</strong>}
                  extra={
                    state.myRole === 'promoter' && index === state.providers.length - 1 ?
                    <Button onClick={addProvider}>添加协作方</Button>
                    : null
                  }
                  size="small"
                  {...cardStyles}
                >
                  {
                    !provider.isReady ?
                      state.myRole === 'promoter' ?
                      <ProviderForm /> : <TaskForm ref={taskFormRef} />
                    : <TaskDetail />
                  }
                  <Row className="operation-area">
                    {
                      state.myRole === 'promoter' ?
                      <Button type="primary" onClick={switchProvider}>发起任务</Button> :
                      <>
                        <Space size={30}>
                          <Button
                            type="primary"
                            danger
                            onClick={() => setRefuseOpen(true)}
                          >拒绝</Button>
                          <Button
                            type="primary"
                            onClick={() => providerPass(index)}
                          >通过</Button>
                        </Space>
                        <RefuseModal
                          open={refuseOpen}
                          onCancel={() => setRefuseOpen(false)}
                        />
                      </>
                    }
                  </Row>
                </Card>
              </Col>
            )
          })
          : null
        }
      </Row>
    </>
  );
};

export default Task;
