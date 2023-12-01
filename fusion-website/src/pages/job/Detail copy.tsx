import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Col } from 'antd';
import { useImmer } from 'use-immer';
import TaskDetail from "./components/TaskDetail";
import TaskProgress from "./components/TaskProgress";
import { useParams } from "@umijs/max";
import './index.less';

const Detail = () => {
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

  const [state, setState] = useImmer({
    isReady: false,
    myRole: 'promoter',
    providers: [{
      isReady: false
    }]
  });

  return (
    <>
      <Row>
        <Col span={24 / (state.providers?.length + 1)}>
          <Card
            title={promoterTitle}
            size="small"
            {...cardStyles}
          >
            <TaskDetail />
          </Card>
        </Col>
        {
          state.providers.length ?
          state.providers.map((provider, index) => {
            return (
              <Col key={index} span={24 / (state.providers.length + 1)}>
                <Card
                  title={<strong>协作方 10.10.102.233:90</strong>}
                  size="small"
                  {...cardStyles}
                >
                  <TaskDetail />
                </Card>
              </Col>
            )
          })
          : null
        }
      </Row>
      <TaskProgress />
    </>
  );
};

export default Detail;
