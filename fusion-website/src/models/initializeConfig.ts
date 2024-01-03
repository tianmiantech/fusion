import { useImmer } from 'use-immer';
import { useState } from 'react';
import { checkIsInitialized,getGenerateSm2KeyPair } from './service'
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
    const [encryptPublicKey,setEncryptPublicKey] = useState<string>('')
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

    //获取加密公钥
    const getEncryptPublicKey = async ()=>{
        if(encryptPublicKey){
            return encryptPublicKey
        } else {
            const res = await getGenerateSm2KeyPair()
            const {code,data} = res 
            if (code === 0) {
                const public_key = lodash.get(data,'public_key','')
                setEncryptPublicKey(public_key)
                return public_key;
            } else {
                return '';
            }
        }
    }

    return {
        checkInitialize,
        IsInitialized,
        getEncryptPublicKey,
        
    }
}
export default useCheckInitializedStore