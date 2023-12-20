import { createModel } from "hox";
import { useEffect } from "react";
import { useImmer } from "use-immer";
import lodash from 'lodash'
import { useRequest } from "ahooks";
import { queryBloomFilterList } from "../service";

export interface SuggestListItemInterface {
    add_method?:string,
    created_time?:number,
    description?:string,
    hash_configs?:{
        list:{
            columns:string[],
            method:string,
        }[]
    },
    id:string,
    name?:string,
    sql?:string,
    storage_dir?:string,
    storage_size?:number,
    total_data_count?:number
    updated_time?:number
}
interface BloomFilterConfigInterface {
    suggestList:SuggestListItemInterface[]
}
const useBloomFilterFormItem = ()=>{


    const [bloomFilterConfig, setBloomFilterConfig] = useImmer<BloomFilterConfigInterface>({
        suggestList:[]
    })


    const checkBloomFilterList = async ()=>{
        if(bloomFilterConfig.suggestList.length == 0){
            const {code,data} = await queryBloomFilterList({page_size:1000,name:'',page_index:0})
            if(code == 0){
                const listData = lodash.get(data,'list',[])
                setBloomFilterConfig(draft=>{
                    draft.suggestList = listData
                })
            }   
        } 
        return bloomFilterConfig.suggestList
        
    }

    return {
        bloomFilterConfig,
        checkBloomFilterList
    }

}
export default createModel(useBloomFilterFormItem)
