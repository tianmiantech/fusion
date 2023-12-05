import { useEffect, useState, useRef } from "react";
import { Descriptions } from 'antd';
import useDetail from "../../hooks/useDetail";
import {dataResourceTypeMap} from '@/constant/dictionary'
import lodash from 'lodash'
import moment from 'moment'

interface hashConfigItemInterface {
  columns: string[];
  method: string;
}
const TaskDetail = () => {
  
  const {detailData} = useDetail();

  const labelStyle = {
    width: 150,
    justifyContent: 'end'
  }


  const renderHashConfig = () => {
    const list = lodash.get(detailData,'jobDetailData.partner.hash_config.list',[])
    let result = ''
    list.forEach((item:hashConfigItemInterface,index:number)=>{
      result += `${item.method}(${item.columns.join('+')})`
      if(index !== list.length-1){
        result += '+'
      }
    })
    return <>
      {result}
    </>
  }

  const renderUpdateTime = ()=>{
    const created_time = lodash.get(detailData,'jobDetailData.partner.created_time','')
    const updated_time = lodash.get(detailData,'jobDetailData.partner.updated_time','')
    return moment(updated_time|| created_time||new Date()).startOf('hour').fromNow()
  }



  return (
    <>
      <Descriptions column={1} bordered labelStyle={labelStyle}>
        <Descriptions.Item label="服务地址">{lodash.get(detailData,'jobDetailData.partner.base_url','') }</Descriptions.Item>
        <Descriptions.Item label="样本类型">{ dataResourceTypeMap.get(lodash.get(detailData,'jobDetailData.partner.data_resource_type',''))  }</Descriptions.Item>
        <Descriptions.Item label="数据量">{ lodash.get(detailData,'jobDetailData.partner.total_data_count','')}</Descriptions.Item>
        <Descriptions.Item label="主键">{renderHashConfig()}</Descriptions.Item>
        <Descriptions.Item label="公钥">{ lodash.get(detailData,'jobDetailData.partner.public_key','')}</Descriptions.Item>
        <Descriptions.Item label="更新时间">{renderUpdateTime()}</Descriptions.Item>
      </Descriptions>
    </>
  );
};

export default TaskDetail;
