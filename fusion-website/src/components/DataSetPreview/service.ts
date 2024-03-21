import {request}from '@/utils/request'
export interface FileMergeInterface {
    filename:string,
    identifier:string,
    upload_file_use_type:string
}

interface FileMergeReponseInterface {
    code: number,
    data:{
      filename:string,
      scan_session_id:string
    }
}
/**
 * 文件分块上传后的合并
 * @param params 
 * @returns 
 */ 
export  const  fileMerge =  (params:FileMergeInterface):Promise<FileMergeReponseInterface>=>{
    return request.post("file/merge",params)
}

/**
 * 文件安全扫描
 */
interface FilleScanReponseInterface {
    code:number,
    data:{
        scan_result:{
            finished:string,
            success:string,
            message:string
        }
    }
}
export const securityScan = (scan_session_id:string):Promise<FilleScanReponseInterface>=>{
    return request.post("file/scan_result",{scan_session_id})
}

export interface PeviewDataRequestInterface {
    data_source_params?:any,
    database_type?:'MySQL'|'PostgreSQL'|'Doris'|'ClickHouse'|'Oracle'|'SQLServer'|'Hive',
    data_source_file?:string,
    add_method:'HttpUpload'|'LocalFile'|'Database',
    sql?:string

}
export const preViewData = (params:PeviewDataRequestInterface)=>{
    return request.post("data_source/preview",params)
}