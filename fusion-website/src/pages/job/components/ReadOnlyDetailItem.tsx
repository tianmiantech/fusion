import { useEffect, useState, useRef } from "react";
import { Descriptions,Typography,Button,Popover } from 'antd';
import {dataResourceTypeMap} from '@/constant/dictionary'
import lodash from 'lodash'
import moment from 'moment'
import {Progress,List} from 'antd'
import JobCard from './JobCard'
import { HttpUploadPreview } from "@/components/DataSetPreview";
import { ROLE_TYPE } from "@/constant/dictionary";
interface hashConfigItemInterface {
  columns: string[];
  method: string;
}

interface ReadOnlyDetailItemProps {
  title?:string,
  bodyStyle?:any,
  detailInfoData:{
    hash_config?:{list:any[]}
    base_url?:string,
    data_resource_type?:string,
    table_data_resource_info?:any|null,
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
  const {detailInfoData,progressData,title,bodyStyle } = props
  
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
    const start_time = lodash.get(detailInfoData,'start_time','')
    return moment(start_time||new Date()).startOf('hour').fromNow()
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
        {renderLogBtn()}
    </>
  }
  const renderLogBtn = ()=>{
    const logList = lodash.get(progressData,'logs',[])
    if(logList.length>0){
      return <>
      <br/>
      <Popover content={<div>{arrayToParagraphs(logList)}</div>}><Button>查看日志</Button></Popover>
      </>

    }
    return null
  }

  function arrayToParagraphs(array:string[]) {
    return array.map(function(item) {
      return <p>{item}</p>;
    });
  }

  const renderTotlDataCount = ()=>{
    const total_data_count = lodash.get(detailInfoData,'total_data_count','')
    const table_data_resource_info = lodash.get(detailInfoData,'table_data_resource_info',null)
    const role = lodash.get(detailInfoData,'role',{})
    if(role===ROLE_TYPE.PROMOTER && table_data_resource_info){
      const fileName = lodash.get(table_data_resource_info,'data_source_file','')
      return <>
      {total_data_count}
      <HttpUploadPreview filename={fileName} />
    </>
    }
    return <>
       {total_data_count}
    </>
  }

  return (<JobCard title={title} bodyStyle={bodyStyle}>
      <Descriptions column={1} bordered labelStyle={labelStyle}>
        <Descriptions.Item label="服务地址">{lodash.get(detailInfoData,'base_url','') }</Descriptions.Item>
        <Descriptions.Item label="样本类型">{ dataResourceTypeMap.get(lodash.get(detailInfoData,'data_resource_type',''))  }</Descriptions.Item>
        <Descriptions.Item label="数据量">{ renderTotlDataCount()}</Descriptions.Item>
        <Descriptions.Item label="主键">{renderHashConfig()}</Descriptions.Item>
        <Descriptions.Item label="开始时间">{renderUpdateTime()}</Descriptions.Item>
        {progressData?<Descriptions.Item label="任务进度">{renderCurrentProgress()} </Descriptions.Item>:null}
      </Descriptions>
    </JobCard>
  );
};

export default ReadOnlyDetailItem;
