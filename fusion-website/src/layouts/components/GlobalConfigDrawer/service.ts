import {request}from '@/utils/request'

export const getGlobalConfig = () => {
    return request.get('global_config/get',{groups:'fusion'})
}

export const testMySelfConnect = (base_url:string) => {
    return request.post('global_config/test_connect',{base_url})
}

export interface  UpdateGlobalConfigRequestInterface {
    groups:{
        fusion:{
            [key:string]:string
        }
    }
}
export const updateGlobalConfig = (params:UpdateGlobalConfigRequestInterface) => {
    return request.post('global_config/update',params)
}