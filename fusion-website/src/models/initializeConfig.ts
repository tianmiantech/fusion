import { useImmer } from 'use-immer';
import { useState } from 'react';
import { checkIsInitialized } from './service'
import {useRequest} from 'ahooks'
import lodash from 'lodash'
import {FUNSION_INITIALIZED_KEY} from '@/constant/dictionary'

interface InitializedReponse {
    code:number,
    data:{
        initialized:boolean
    }
}
/**
 * 检查项目是否有进行初始化，如果没有则需要进行初始化才能使用
 * @returns 
 */
const useCheckInitializedStore = ()=> {
    const [IsInitialized,setIsInitialized] = useState<boolean>(false)
    //项目加载时请求一次，标记是否请求过了 
    const [isRequested,setIsRequested] = useState(false)

    const checkInitialize = async ()=>{
        //第一次检查则请求接口
        if(!isRequested) {
            let initialized = false
            const req:InitializedReponse = await checkIsInitialized();
            const{code} = req
            if(code == 0){
                initialized = lodash.get(req,'data.initialized',false);
                //将初始化状态存储到本地 方便某些函数中判断是否初始化
                localStorage.setItem(FUNSION_INITIALIZED_KEY,JSON.stringify(initialized))
                //表示没有初始化，需要进行初始化
                setIsInitialized(initialized)
                setIsRequested(true)
            }
            return initialized;
        } else {
            return IsInitialized
        }
    }

    return {
        checkInitialize,
        IsInitialized
    }
}
export default useCheckInitializedStore