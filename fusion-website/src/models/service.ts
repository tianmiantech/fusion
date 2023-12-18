import {request}from '@/utils/request'

//检查是否有进行初始化
export const checkIsInitialized=()=>{
    return request.get("/service/is_initialized")
}

//获取密码加密公钥
export const getGenerateSm2KeyPair=()=>{
    return request.get("/crypto/generate_sm2_key_pair")
}