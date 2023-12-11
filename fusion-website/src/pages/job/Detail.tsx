import { useEffect, useState, useRef, useMemo } from "react";
import { Card, Row, Spin } from 'antd';
import Promoter from "./components/Promoter";
import Provider from './components/Provider'
import DetailWithProgress from './components/DetailWithProgress'
import useDetail from './hooks/useDetail'
import { useParams } from "@umijs/max";
import { useUnmount } from "ahooks";
import styles from './index.less'
import lodash from 'lodash'

const Detail = () => {
  
  const { id:jobId } = useParams<{id: string}>();

  const {detailData,setDetailData,clearDetailData} = useDetail();

  const [role,setRole] = useState<string>('');
  
  useEffect(()=>{
    if(jobId){
      setDetailData(draft=>{
        draft.jobId = jobId;
      })
    }
  },[jobId]) 
  
  useUnmount(()=>{
    clearDetailData()
  })
  
  useEffect(()=>{
    if(detailData.role){
      setRole(detailData.role)
    }
  },[role])

const renderLoading = ()=>{
  return <div className={styles.loadingContainer}>
    <Spin size="large" tip='加载中...'></Spin>
  </div>
}

const renderDetail = ()=>{
  const status = lodash.get(detailData,'jobDetailData.status','')
  if(!status|| status === 'editing' || status === 'auditing') {
    if(detailData.role === 'promoter'){
      return <Promoter />
    }else if(detailData.role === 'provider'){
      return <Provider />
    }
  } else if(status) {
    return <DetailWithProgress />
  }
  return renderLoading();
}

  return (
    <>
     {renderDetail()}
    </>
  );
};

export default Detail;
