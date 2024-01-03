import {request}from '@/utils/request'

/**
 * 获取可用的数据源类型
 * @returns 
 */
export const getPrevResult = (id:string) => {
    return request.get(`/job/result/preview`, {id})
}

/**
 * 下载结果
 * @param id 
 * @returns 
 */
export const downloadResult = (id:string) => {
    return request.get(`/job/result/download`, {id},{parseResponse:false,skipAllError:true})
}