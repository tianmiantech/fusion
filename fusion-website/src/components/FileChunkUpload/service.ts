import {request}from '@/utils/request'
export interface FileMergeInterface {
    filename:string,
    identifier:string,
    upload_file_use_type:string
}
export  const  fileMerge =  (params:FileMergeInterface)=>{
    return request.post("file/merge",params)
}