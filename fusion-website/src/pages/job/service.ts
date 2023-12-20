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
    data_source_params:any
}
export const testDataSource = (parmas:TestDataSourceInterface)=>{
    return request.post('/data_source/test',parmas)
} 

export interface CreateJobRequestInterface {
    remark?:string,
    data_resource:{
        bloom_filter_resource_input?:{
            bloom_filter_id:string
        },
        table_data_resource_info?:{
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
export const createJob = (parmas:CreateJobRequestInterface) => {
    return request.post('/job/create',parmas)
}


export interface TestPartnerConntentRequestInterface {
    base_url:string,
    name?:string,
    public_key:string
}
/**
 * 测试协作方是否能联通
 * @param parmas 
 * @returns 
 */
export const testPartnerConntent = (parmas:TestPartnerConntentRequestInterface) => {
    return request.post('/member/test_connect',parmas)
}

export interface SendTaskToProviderRequestInterface {
    job_id:string,
    base_url:string,
    member_name?:string
    public_key:string
}
/**
 * 发送任务到协作方
 * @param parmas 
 * @returns 
 */
export const sendJobToProvider = (parmas:SendTaskToProviderRequestInterface) => {
    return request.post('/job/send_to_provider',parmas)
}

export const getMemberList = (name?:string)=>{
    return request.get('/member/list',{name})
}

/**
 * 获取任务详情
 */
export const getJobDetail = (id:string)=>{
    return request.get('/job/detail',{id})
}

/**
 * 协作方同意任务
 */
export const agreeAndStart = (param:CreateJobRequestInterface)=>{
    return request.post('/job/agree_and_start',param)
}

/**
 * 协作方任务，拒绝
 */
export interface DisagreeJobRequestInterface {
    job_id:string,
    reason?:string
}
export const disagreeJob = (param:DisagreeJobRequestInterface)=>{
    return request.post('/job/disagree',param)
}



/**
 * 获取多方任务进度
 * @param job_id 
 * @returns 
 */
export const getMergedJobProgress = (job_id:string)=>{
    return request.get('/job/get_merged_job_progress',{job_id})
}

/**
 * 
 * @param job_id 
 * @returns 
 */
export const getMyJobProgress = (job_id:string)=>{
    return request.get('/job/get_my_job_progress',{job_id})
}

export interface QueryBloomFilterListRequestInterface {
    page_size:number,
    name:string,
    page_index:number
}
export const queryBloomFilterList= (params:QueryBloomFilterListRequestInterface)=>{
    return request.post('/bloom_filter/query',params)
}

