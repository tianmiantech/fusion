import {request}from '@/utils/request'

//检查是否有进行初始化
export const checkIsInitialized=()=>{
    return request.get("/service/is_initialized")
}