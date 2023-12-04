import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Col } from 'antd';
import { useImmer } from 'use-immer';
import TaskDetail from "./components/TaskDetail";
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
  
  useEffect(()=>{
    if(jobId){
      setDetailData(draft=>{
        draft.jobId = jobId;
      })
    }
  },[jobId])
  

  

  return (
    <>
     {detailData.role === 'promoter'?<Promoter />:<Provider/>}
    </>
  );
};

export default Detail;
