import {request}from '@/utils/request'

export const getGlobalConfig = () => {
    return request.get('global_config/get',{groups:'fusion'})
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