import {createJob,CreateJobRequestInterface} from '../service'
import { useRequest } from "ahooks";
import { useImmer } from 'use-immer';
import lodash from 'lodash'

const useJobForm = ()=>{

    const [jobFormData,setJobFormData] = useImmer({
        job_id:'',//创建任务成功的id
        initialValues:{
            remark:'',
            status:"editing",
            data_resource_type:'',
            hash_config:{},
            table_data_resource_info:{},
            dataSetAddMethod:'HttpUpload',
            bloom_filter_id:'',
        }
    })

    const {run:runCreateJob,loading:createJobloading} = useRequest(async (params:CreateJobRequestInterface)=>{
        const reponse = await createJob(params)
        const {code,data} = reponse;
        if(code === 0){
            setJobFormData(draft=>{
                draft.job_id = lodash.get(data,'job_id');
            })
        }else{
            console.log("创建任务失败",reponse);
        }
        console.log("reponse",reponse);
        
    },{
        manual:true
    })
    return {
        setJobFormData,
        jobFormData,
        runCreateJob,
        createJobloading
    }
}
export default useJobForm
