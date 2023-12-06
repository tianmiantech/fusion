import {request}from '@/utils/request'

export interface GetJobListRequestInterface {
    page_size:number,
    status?:'editing'|'disagree'|'wait_run'|'running'|'stop_on_running'|'error_on_running'|'success',
    job_id?:string,
    role:'promoter'|'provider',
    page_index:number
}
export const getJobList = (params:GetJobListRequestInterface) => {
    return request.get('/job/query',{params})
}

export const deleteJob = (id:string) => {
    return request.post(`/job/delete`,{id})
}