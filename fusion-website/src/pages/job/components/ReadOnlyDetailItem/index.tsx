import { useEffect, useState, useRef,useContext, } from "react";
import { Descriptions,Typography,Button,Popover,ConfigProvider, Steps } from 'antd';
import {dataResourceTypeMap} from '@/constant/dictionary'
import lodash from 'lodash'
import moment from 'moment'
import {Progress,List} from 'antd'
import JobCard from '../JobCard'
import { DataPreviewBtn } from "@/components/DataSetPreview";
import { ROLE_TYPE } from "@/constant/dictionary";
import styles from './index.less'

interface hashConfigItemInterface {
  columns: string[];
  method: string;
}


export interface ReadOnlyDetailItemDeetailInfoDataInterface {
  hash_config?:{list:any[]}
  base_url?:string,
  data_resource_type?:string,
  table_data_resource_info?:any|null,
  total_data_count?:string,
  public_key?:string,
  created_time?:string,
  updated_time?:string,
}
interface ReadOnlyDetailItemProps {
  title?:string,
  bodyStyle?:any,
  detailInfoData:ReadOnlyDetailItemDeetailInfoDataInterface|null,
}
const ReadOnlyDetailItem = (props:ReadOnlyDetailItemProps) => {
  const {detailInfoData,title,bodyStyle } = props
  const configContext = useContext(ConfigProvider.ConfigContext);
  const prefixCls = 'portal' || configContext.getPrefixCls();
  const labelStyle = {
    width: 150,
    justifyContent: 'end',
    height: '20px',
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
      <DataPreviewBtn requestParams={{data_source_file:fileName,add_method:'HttpUpload'}} />
    </>
    }
    return <>
       {total_data_count}
    </>
  }

  return (<JobCard title={title} bodyStyle={bodyStyle} >
      <Descriptions column={1} bordered labelStyle={labelStyle} contentStyle={{paddingTop:10,paddingBottom:10}}>
        <Descriptions.Item label="服务地址">{lodash.get(detailInfoData,'base_url','') }</Descriptions.Item>
        <Descriptions.Item label="样本类型">{ dataResourceTypeMap.get(lodash.get(detailInfoData,'data_resource_type',''))  }</Descriptions.Item>
        <Descriptions.Item label="数据量" >{ renderTotlDataCount()}</Descriptions.Item>
        <Descriptions.Item label="主键">{renderHashConfig()}</Descriptions.Item>
      </Descriptions>
      
    </JobCard>
  );
};

export default ReadOnlyDetailItem;
