import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Spin } from 'antd';
import { useImmer } from 'use-immer';
import TaskDetail from "./components/Provider/PrompoterDetail";
import TaskProgress from "./components/TaskProgress";

import { useRequest } from "ahooks";

import Promoter from "./components/Promoter";
import Provider from './components/Provider'
import { useModel } from "@umijs/max";
import lodash from 'lodash'
import useDetail from './hooks/useDetail'
import { useParams } from "@umijs/max";

const Detail = () => {
  
  const { id:jobId } = useParams<{id: string}>();

  const {detailData,setDetailData} = useDetail();

  const [role,setRole] = useState<string>('');
  
  useEffect(()=>{
    if(jobId){
      setDetailData(draft=>{
        draft.jobId = jobId;
      })
    }
  },[jobId])
  
  useEffect(()=>{
    if(detailData.role){
      setRole(detailData.role)
    }
  },[role])

const renderDetail = ()=>{
  if(detailData.role === 'promoter'){
    return <Promoter />
  }else if(detailData.role === 'provider'){
    return <Provider />
  } else
  return <Spin></Spin>;
}

  return (
    <>
     {renderDetail()}
    </>
  );
};

export default Detail;
