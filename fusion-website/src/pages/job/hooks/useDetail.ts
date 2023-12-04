
import { createModel } from "hox";
import { useEffect } from "react";
import { useImmer } from "use-immer";
import lodash from 'lodash'
import { useRequest } from "ahooks";

import { getJobDetail } from "../service";

interface useDetailDataInterface {
    role:'promoter'|'provider',
    status?:string,// 审核状态
    jobId?:string,
    jobDetailData:any,
  }

const useDetail = ()=>{
    
    const [detailData, setDetailData] = useImmer<useDetailDataInterface>({
        role:'promoter',
        jobDetailData:null,
        jobId:''
    });

    useEffect(() => {
        if(detailData.jobId){
          runGetJobDetail(detailData.jobId);
        }
    },[detailData.jobId])

    const {run:runGetJobDetail} = useRequest(async (id:string)=>{
        const res = await getJobDetail(id);
        const {code,data} = res;
        if(code === 0){
          const {role } = data
          setDetailData(draft=>{
            draft.role = role;
            draft.jobDetailData = data
          })
        }
    }, {manual:true})

    return {
        detailData,
        setDetailData
    }

}
export default createModel(useDetail);