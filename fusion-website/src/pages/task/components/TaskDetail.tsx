import { useEffect, useState, useRef } from "react";
import { Descriptions } from 'antd';

const TaskDetail = () => {
  const [detailInfo, setDetailInfo] = useState({
    dataResourceType: '数据集',
    count: 1000,
    hashKey: 'MD5(id + x0) + SHA256(x2)',
    remark: '任务备注'
  });
  const labelStyle = {
    width: 100,
    justifyContent: 'end'
  }

  return (
    <>
      <Descriptions column={1} labelStyle={labelStyle}>
        <Descriptions.Item label="样本类型">{ detailInfo.dataResourceType }</Descriptions.Item>
        <Descriptions.Item label="样本量">{ detailInfo.count }</Descriptions.Item>
        <Descriptions.Item label="主键">{ detailInfo.hashKey }</Descriptions.Item>
        <Descriptions.Item label="备注">{ detailInfo.remark }</Descriptions.Item>
      </Descriptions>
    </>
  );
};

export default TaskDetail;
