import {request}from '@/utils/request'
import {HashFormValue} from './components/HashForm/index'
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

export interface CreateJobInterface {
    remark?:string,
    data_resource:{
        bloom_filter_resource_input?:{
            bloom_filter_id:string
        },
        table_data_resource_input?:{
            add_method:'HttpUpload'|'LocalFile'|'Database',
            sql?:string,
            data_source_file?:string,
            database_type?:'MySQL'|'PostgreSQL'|'Hive'|'ClickHouse'|'Oracle'|'SQLServer'|'MongoDB'|'Redis'|'HBase'|'Cassandra'|'Doris'|'ElastiSearch'|'Kafka'|'Kudu',
            data_source_params?:Map<String,Object>
        },
        data_resource_type:'TableDataSource'|'PsiBloomFilter'
        hash_config?:{list:HashFormValue[]}
    },
}
/**
 * 创建任务
 * @param parmas 
 * @returns 
 */
export const createJob = (parmas:CreateJobInterface) => {
    return request.post('/job/create',parmas)
}