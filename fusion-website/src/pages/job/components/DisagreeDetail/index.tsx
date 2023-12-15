
import { useEffect } from 'react';
import ReadOnlyDetailItem from '../ReadOnlyDetailItem'
import useDetail from "../../hooks/useDetail";
import lodash from 'lodash'
import { useImmer } from "use-immer";
import { Card, Row, Col, Button,message } from 'antd';
import JobCard from '../JobCard'
import SendJobForm from "../SendJobForm";
import { ROLE_TYPE } from '@/constant/dictionary';

const Index = ()=>{
  const { detailData }= useDetail()
  const [data,setData] = useImmer({
    promoterData: null,
    providerData: null,
  })

  useEffect(()=>{
    if(detailData.jobDetailData){
      const role = lodash.get(detailData,'jobDetailData.role')
      const partner = lodash.get(detailData,'jobDetailData.partner')
      const myself = lodash.get(detailData,'jobDetailData.myself')
      //表示当前角色为发起方
      if(role === ROLE_TYPE.PROMOTER){
        setData(draft=>{
          draft.promoterData = myself
          draft.providerData = partner
        })
      } else {
        setData(draft=>{
          draft.promoterData = partner
          draft.providerData = myself
        })
      }
    }
  },[detailData.jobDetailData])

  const renderTitle = (role:string)=>{
    let title = ''
    if(role === ROLE_TYPE.PROMOTER){
      const member_id = lodash.get(data,'promoterData.member_id')
      if (member_id) {
        title = `发起方（${member_id}）`
      }
      else {
        title = '发起方'
      }
    } else {
      const member_id = lodash.get(data,'providerData.member_id')
      if (member_id) {
        title = `协作方（${member_id}）`
      } else {
        title = '协作方'
      }
    }
    return title
  }


    return <>
        <Row>
            <Col span={12}>
                <JobCard
                  title={renderTitle(ROLE_TYPE.PROMOTER)}
                  bodyStyle={{ height: 'calc(100vh - 92px)',}}
                >
                  <ReadOnlyDetailItem detailInfoData={data.promoterData}/>
                </JobCard>
            </Col>
            <Col span={12}>
                <JobCard
                  title={renderTitle(ROLE_TYPE.PROVIDER)}
                  bodyStyle={{ height: 'calc(100vh - 92px)',}}
                >
                  <SendJobForm showActionButton={false}/>
                </JobCard>
            </Col>
        </Row>
    </>
}
export default Index