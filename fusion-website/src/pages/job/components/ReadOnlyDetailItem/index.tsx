import { useEffect, useState, useRef,useContext, ReactNode} from "react";
import { Descriptions,Typography,Button,Popover,ConfigProvider, Steps } from 'antd';
import {dataResourceTypeMap} from '@/constant/dictionary'
import lodash from 'lodash'
import JobCard from '../JobCard'
import { DataPreviewBtn } from "@/components/DataSetPreview";
import { ROLE_TYPE } from "@/constant/dictionary";
import useDetail from "../../hooks/useDetail";
import {renderHashConfig} from '@/utils/utils'

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
  cardExtra?:ReactNode
}
const ReadOnlyDetailItem = (props:ReadOnlyDetailItemProps) => {
  const {detailData} = useDetail();
  const {detailInfoData,title,bodyStyle,cardExtra=null } = props
  const configContext = useContext(ConfigProvider.ConfigContext);
  const prefixCls = 'portal' || configContext.getPrefixCls();
  const labelStyle = {
    width: 150,
    justifyContent: 'end',
    height: '20px',
  }


  const renderTotlDataCount = ()=>{
    const total_data_count = lodash.get(detailInfoData,'total_data_count','')
    const table_data_resource_info = lodash.get(detailInfoData,'table_data_resource_info',null)
    const role = lodash.get(detailInfoData,'role',{})
    if(role===ROLE_TYPE.PROMOTER && table_data_resource_info){
      const fileName = lodash.get(table_data_resource_info,'data_source_file','')
      return <>
      {total_data_count}
      <DataPreviewBtn requestParams={getDataPrevieParams()} />
    </>
    }
    return <>
       {total_data_count}
    </>
  }

  const getDataPrevieParams = ()=>{
    const table_data_resource_info = lodash.get(detailInfoData,'table_data_resource_info',null)
    const add_method = lodash.get(table_data_resource_info,'add_method','')
    //文件上传
    if(add_method === 'HttpUpload'){
      const fileName = lodash.get(table_data_resource_info,'data_source_file','')
      return {data_source_file:fileName,add_method}
    //数据库
    }else if(add_method ==='Database'){ 
      return {
        ...table_data_resource_info
      }
    }
  }

  return (<JobCard title={title} bodyStyle={bodyStyle} extra={cardExtra}>
      <Descriptions column={1} bordered labelStyle={labelStyle} contentStyle={{paddingTop:10,paddingBottom:10}}>
        <Descriptions.Item label="服务地址">{lodash.get(detailInfoData,'base_url','') }</Descriptions.Item>
        <Descriptions.Item label="样本类型">{ dataResourceTypeMap.get(lodash.get(detailInfoData,'data_resource_type',''))  }</Descriptions.Item>
        <Descriptions.Item label="数据量" >{ renderTotlDataCount()}</Descriptions.Item>
        <Descriptions.Item label="主键">{renderHashConfig(lodash.get(detailInfoData,'hash_config'))}</Descriptions.Item>
        <Descriptions.Item label="附加结果字段">{lodash.get(detailInfoData,'additionalResultColumns') }</Descriptions.Item>
      </Descriptions>
    </JobCard>
  );
};

export default ReadOnlyDetailItem;
