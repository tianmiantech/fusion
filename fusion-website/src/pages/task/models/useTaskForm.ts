import {createJob,CreateJobInterface} from '../service'
import { useRequest } from "ahooks";
import { useImmer } from 'use-immer';
import lodash from 'lodash'

const useTaskForm = ()=>{

    const [taskFormData,setTaskFormData] = useImmer({
        job_id:''//创建任务成功的id,

    })

    const {run:runCreateJob,loading:createJobloading} = useRequest(async (params:CreateJobInterface)=>{
        const reponse = await createJob(params)
        const {code,data} = reponse;
        if(code === 0){
            setTaskFormData(draft=>{
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
        taskFormData,
        runCreateJob,
        createJobloading
    }
}
export default useTaskForm
