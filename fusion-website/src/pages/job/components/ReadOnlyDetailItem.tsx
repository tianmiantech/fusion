import { useEffect, useState, useRef } from "react";
import { Descriptions, } from 'antd';
import {dataResourceTypeMap} from '@/constant/dictionary'
import lodash from 'lodash'
import moment from 'moment'
import {Progress,List} from 'antd'
import CurrentJobProgress from './DetailWithProgress/CurrentJobProgress'

interface hashConfigItemInterface {
  columns: string[];
  method: string;
}

interface ReadOnlyDetailItemProps {
  detailInfoData:{
    hash_config?:{list:any[]}
    base_url?:string,
    data_resource_type?:string,
    total_data_count?:string,
    public_key?:string,
    created_time?:string,
    updated_time?:string,

  },
  progressData?:{
    job_phase?:string, //所处阶段
    end_time?:number, //结束时间
    cost_time?:number, //耗时  毫秒
    logs?:string[],//日志
    status?:'doing'|'completed'|'failed', //状态
    message?:string,
    percent?:number,//进度百分比
    speed_in_second?:number //每秒速度
  }
}
const ReadOnlyDetailItem = (props:ReadOnlyDetailItemProps) => {
  const {detailInfoData,progressData } = props
  
  const labelStyle = {
    width: 150,
    justifyContent: 'end'
  }


  const renderHashConfig = () => {
    const list = lodash.get(detailInfoData,'hash_config.list',[])
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
    const created_time = lodash.get(detailInfoData,'created_time','')
    const updated_time = lodash.get(detailInfoData,'updated_time','')
    return moment(updated_time|| created_time||new Date()).startOf('hour').fromNow()
  }

  const displayChineseTime = (cost_time:number)=>{
    if (typeof cost_time !== 'number' || isNaN(cost_time) || cost_time < 0) {
      return '';
    }
    const milliseconds = cost_time % 1000;
    const seconds = Math.floor((cost_time / 1000) % 60);
    const minutes = Math.floor((cost_time / (1000 * 60)) % 60);
    const hours = Math.floor((cost_time / (1000 * 60 * 60)) % 24);

    let result = '';

    if (hours > 0) {
        result += hours + '小时';
    }

    if (minutes > 0) {
        result += minutes + '分钟';
    }

    if (seconds > 0 || (result === '' && milliseconds > 0)) {
        result += seconds + '秒';
    }

    if (milliseconds > 0 && result === '') {
        result += milliseconds + '毫秒';
    }

    return result;
  }

  const renderCurrentProgress = ()=>{
    const logList = lodash.get(progressData,'logs',[])
    return <>
        任务进度：<Progress style={{width:'70%'}} percent={lodash.get(progressData,'percent',0)} />
      <br/>
        耗时：{displayChineseTime(lodash.get(progressData,'cost_time',0))}
      <br/>
        速度：{lodash.get(progressData,'speed_in_second',0)}条/秒
      <br/>
        结束时间：{moment(lodash.get(progressData,'end_time',0)).format('YYYY-MM-DD HH:mm:ss')}
        {logList.length>0?<><br/>日志：<List dataSource={logList}/></>:null}
    </>
  }

  



  return (
    <>
      <Descriptions column={1} bordered labelStyle={labelStyle}>
        <Descriptions.Item label="服务地址">{lodash.get(detailInfoData,'base_url','') }</Descriptions.Item>
        <Descriptions.Item label="样本类型">{ dataResourceTypeMap.get(lodash.get(detailInfoData,'data_resource_type',''))  }</Descriptions.Item>
        <Descriptions.Item label="数据量">{ lodash.get(detailInfoData,'total_data_count','')}</Descriptions.Item>
        <Descriptions.Item label="主键">{renderHashConfig()}</Descriptions.Item>
        <Descriptions.Item label="公钥">{ lodash.get(detailInfoData,'public_key','')}</Descriptions.Item>
        <Descriptions.Item label="更新时间">{renderUpdateTime()}</Descriptions.Item>
        {progressData?<Descriptions.Item label="任务进度">{renderCurrentProgress()} </Descriptions.Item>:null}
        
      </Descriptions>
    </>
  );
};

export default ReadOnlyDetailItem;
