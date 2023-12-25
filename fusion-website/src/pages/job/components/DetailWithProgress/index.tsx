import { useEffect, useState, useRef } from "react";
import { Row,Col,Card, Space,Button, Alert,message } from 'antd';
import useDetail from "../../hooks/useDetail";
import JobCard from '../JobCard'
import ReadOnlyDetailItem from '../ReadOnlyDetailItem'
import JobProgress from "./JobProgress";
import {ROLE_TYPE,JOB_STATUS} from '@/constant/dictionary'
import lodash from 'lodash'
import { useImmer } from "use-immer";
import { useRequest } from "ahooks";
import { getPrevResult,downloadResult } from "./service";
import {ResizableTable} from '@/components/DataSetPreview'
import './index.less'
import { restartJob } from '@/pages/home/service'


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
      promoterPhasesList:[],//
      providerPhasesList:[],
      previewOpen:false,
      previewData:{
        columns:[],
        dataSource:[]
      }

    })
    //根据任务角色将我方和合作方的数据转换为发起方和参与方的数据
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
      if(detailData.myselfPhasesList.length>0){
        const tmpProgress = lodash.get(detailData,'myselfPhasesList',[]);
        if(detailData.role === ROLE_TYPE.PROMOTER){
          setData(g=>{
            g.promoterPhasesList = tmpProgress;
          })
        } else {
          setData(g=>{
            g.providerPhasesList = tmpProgress;
          })
        } 
      }
    },[detailData.myselfPhasesList])


    useEffect(()=>{
      if(detailData.partnerJobCurrentProgress){
        const tmpProgress = lodash.get(detailData,'partnerPhasesList',[]);
        if(detailData.role === ROLE_TYPE.PROMOTER){
          setData(g=>{
            g.providerPhasesList = tmpProgress;
          })
        } else {
          setData(g=>{
            g.promoterPhasesList = tmpProgress;
          })
        } 
      }
    },[detailData.partnerPhasesList])

    const {run:runGetPrevResult,loading:getPrevResultLoading} = useRequest(async (id:string)=>{
      const reponse = await getPrevResult(id);
      const {code,data} = reponse
      if(code == 0) {
        setData(draft=>{
          const columns = data.header.map((item: string)=>{
            return {
                title:item,
                dataIndex:item,
            }
          }) 
          draft.previewData.columns = columns
          draft.previewData.dataSource = data.rows
          draft.previewOpen = true
        })
      }
      console.log("result",reponse);
      
    },{manual:true})

    const getFileName = (disposition: string) => {
      if (!disposition) {
        return '';
      }
      const reg = /filename=(.*)/;
      const match = reg.exec(disposition);
      if (!match || match.length < 2) {
        return '';
      }
      const fileName = match[1].trim();
      return decodeURI(fileName);
    };

    const {run:runDownloadResult,loading:downloadResultLoading} = useRequest(async (id:string)=>{
      const reponse = await downloadResult(id);
      if(reponse.status == 200){
        const disposition = reponse.headers['content-disposition'];
        const fileName = getFileName(disposition);
        const binaryData = [];
        binaryData.push(reponse.data);
        const url = window.URL.createObjectURL(new Blob(binaryData));
        const link = document.createElement('a');
        link.download = fileName;
        link.href = url;
        link.click();
        try {
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
        } catch (error) {
          console.log("error",error);
        }
       
      }
      return;
    },{manual:true})

    //如果任务运行成功，则
    const renderResultbtn = ()=>{
      if(detailData.jobDetailData && detailData.jobDetailData.status === JOB_STATUS.SUCCESS){
        return <Space>
          <Button loading={getPrevResultLoading} onClick={()=>runGetPrevResult(detailData.jobId)}>预览结果</Button>
          <Button loading={downloadResultLoading}  onClick={()=>{runDownloadResult(detailData.jobId)}}>下载结果</Button>
        </Space>
      }
    }

    const renderErrorMsg = ()=>{
      if(detailData.jobDetailData && detailData.jobDetailData.status === JOB_STATUS.ERROR_ON_RUNNING){
        return <Col span={24} style={{marginBottom:10}}>
          <Alert  type="error" message={detailData.jobDetailData.message} />
          </Col>
      }
    }


    const {run:restartJobData,loading:restartLoading} = useRequest(async (id)=>{
      const reponse = await restartJob(id)
      const {code,data} = reponse;
      if (code == 0) {
          message.success('重启成功');
          setTimeout(()=>{ window.location.reload()},800)
         
      }
  },{manual:true})

    const renderRestartBtn = ()=>{
      return <Button type="link" onClick={()=>{restartJobData(detailData.jobId)}}>重启运行</Button>
    }

    return <>
          <Card loading={restartLoading}>
              <Row>
              {renderErrorMsg()}
                <Col span={12} >
                    <ReadOnlyDetailItem title="发起方" detailInfoData={data.promoterDetail} cardExtra={renderRestartBtn()}/>
                </Col>
                <Col span={12}>
                    <ReadOnlyDetailItem title='协作方'  detailInfoData={data.providerDetail}/>
                </Col>
              </Row>
          </Card>
          <Card title='任务进度' style={{marginTop:8}} extra={renderResultbtn()}>
              <JobProgress promoterPhasesList={data.promoterPhasesList} providerPhasesList={data.providerPhasesList}/>
          </Card>
            
            <ResizableTable
              open={data.previewOpen}
              onCancel={() => {setData(draft=>{draft.previewOpen = false})}}
              columns={data.previewData.columns}
              dataSource={data.previewData.dataSource}
            />
      </>
}
export default Index