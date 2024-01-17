import {request}from '@/utils/request'
export const getAuditingList = ()=>{
    return request.get('/job/list_auditing')
}
