import { useEffect, useState, useRef } from "react";
import { Row,Col,Card } from 'antd';
import useDetail from "../../hooks/useDetail";
import JobCard from '../JobCard'
import ReadOnlyDetailItem from '../ReadOnlyDetailItem'
import JobProgress from "./JobProgress";
import {ROLE_TYPE} from '@/constant/dictionary'
import lodash from 'lodash'
import { useImmer } from "use-immer";
import './index.less'


/**
 * 当前访问数据的角色分为发起方，协作方
 * 任务是由发起方当发起方，发起方发起任务，协作方参与任务，任务的角色是固定的。
 * 
 * @returns 
 */
const Index = ()=>{
    const {detailData } = useDetail();

    const [data,setData] = useImmer({
      promoterDetail:{},
      providerDetail:{},
      promoterProgress:{},
      providerProgress:{},
    })

    useEffect(()=>{
      if(detailData.jobDetailData){
        let promoterDetail = {},providerDetail = {};
        if(detailData.role === ROLE_TYPE.PROMOTER){
          promoterDetail =lodash.get(detailData,'jobDetailData.myself')
          providerDetail = lodash.get(detailData,'jobDetailData.partner')
        } else {
          promoterDetail =lodash.get(detailData,'jobDetailData.partner')
          providerDetail = lodash.get(detailData,'jobDetailData.myself')
        }
        setData(g=>{
          g.promoterDetail = promoterDetail;
          g.providerDetail = providerDetail;
        })
      }
    },[detailData.jobDetailData])

    useEffect(()=>{
      if(detailData.myselfJobProgress){
        const tmpProgress = lodash.get(detailData,'myselfJobProgress');
        if(detailData.role === ROLE_TYPE.PROMOTER){
          setData(g=>{
            g.promoterProgress = tmpProgress;
          })
        } else {
          setData(g=>{
            g.providerProgress = tmpProgress;
          })
        } 
      }
    },[detailData.myselfJobProgress])


    useEffect(()=>{
      if(detailData.partnerJobProgress){
        const tmpProgress = lodash.get(detailData,'partnerJobProgress');
        if(detailData.role === ROLE_TYPE.PROMOTER){
          setData(g=>{
            g.providerProgress = tmpProgress;
          })
        } else {
          setData(g=>{
            g.promoterDetail = tmpProgress;
          })
        } 
      }
    },[detailData.partnerJobProgress])

    useEffect(()=>{
    },[detailData])

    const renderProgressTitle = ()=>{
      return <>任务进度<span style={{fontSize:12,color:'gray'}}>（发起方,协作方节点任务阶段相同）</span></>
    }

    return <>
          <div className="topContainer">
            <Card title={renderProgressTitle()}>
              <JobProgress data={detailData.myselfJobProgress}/>
            </Card>
          </div>
            <Row>
              <Col span={12}>
                  <ReadOnlyDetailItem title="发起方" detailInfoData={data.promoterDetail} progressData={data.promoterProgress}/>
              </Col>
              <Col span={12}>
                  <ReadOnlyDetailItem title={'协作方'}  detailInfoData={data.providerDetail} progressData={data.providerProgress}/>
              </Col>
            </Row>
      </>
}
export default Index