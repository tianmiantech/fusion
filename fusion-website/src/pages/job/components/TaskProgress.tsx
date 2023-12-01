import { useEffect, useRef, useState } from 'react';
import { Row, Col, Descriptions, Progress } from 'antd';

const TaskProgress = () => {
  

  return (
    <Row className="progress-panel">
      <Col span={12} style={{ padding: 20, boxShadow: '0 2px 3px rgb(0 0 0 / 20%)' }}>
        <Descriptions column={1}>
          <Descriptions.Item label="任务进度">
            <Progress percent={30} />
          </Descriptions.Item>
          <Descriptions.Item label="任务状态">进行中</Descriptions.Item>
          <Descriptions.Item label="样本总量">100000</Descriptions.Item>
          <Descriptions.Item label="已处理样本量">30000</Descriptions.Item>
          <Descriptions.Item label="预计剩余时间">1小时30分钟</Descriptions.Item>
        </Descriptions>
      </Col>
      <Col span={12} style={{ padding: 20 }}>
        任务日志
      </Col>
    </Row>
  )
}

export default TaskProgress;
