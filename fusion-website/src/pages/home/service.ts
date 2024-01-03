import {request}from '@/utils/request'

export interface GetJobListRequestInterface {
    page_size:number,
    status?:'editing'|'disagree'|'wait_run'|'running'|'stop_on_running'|'error_on_running'|'success'|'auditing',
    job_id?:string,
    role:'promoter'|'provider',
    page_index:number
}
export const getJobList = (params:GetJobListRequestInterface) => {
    const {page_index} = params //服务器的分页从0开始
    return request.get('/job/query',{...params,page_index:page_index-1})
}

export const deleteJob = (id:string) => {
    return request.post(`/job/delete`,{id})
}

export const restartJob = (id:string) => {
    return request.post(`/job/restart`,{job_id:id})
}