import {request}from '@/utils/request'

/**
 * 获取可用的数据源类型
 * @returns 
 */
export const getDataSourceAvailableType = ()=>{
    return request.get('/data_source/available_type')
}

/**
 * 获取全部的数据源
 * @returns 
 */
export const getDataSourceList = ()=>{
    return request.get('/data_source/list')
}

/**
 * 测试数据源的可用性
 * @returns 
 */
export interface TestDataSourceInterface {
    database_type:string,
    data_source_params:{}
}
export const testDataSource = (parmas:TestDataSourceInterface)=>{
    return request.post('/data_source/test',parmas)
} 