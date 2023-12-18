import { useRequest,useMount } from 'ahooks';
import { getDataSourceAvailableType,getDataSourceList} from '../service'
import { useImmer } from 'use-immer';
import lodash from 'lodash'

interface  dataSoureConfigInterface {
    dataSoureTypeList:{label:string,value:string}[]
    dataSoureSuggestion:[]
}

interface getDataSourceAvailableTypeInterface {
    code:number,
    data:{
        available_type:string[]
    }
}

//数据源列表 
interface getDataSourceListInterface {
    code:number,
    data:{
        list:{
            database_type:string,
            host:string,
            port:number
            connector_config:{
                database_name:string,
                user_name:string,
                password:string
            }
        }[]
    }
}



const useDataSourceForm = ()=>{

    const [dataSoureConfig,setDataSourceConfig] = useImmer<dataSoureConfigInterface>({
        dataSoureTypeList:[],
        dataSoureSuggestion:[]
    })


    const {run:runGetDataSourceAvailableType} = useRequest(async ()=>{
        const reponse:getDataSourceAvailableTypeInterface = await getDataSourceAvailableType()
        const {code} = reponse;
        if(code === 0){
            const tmpList = lodash.get(reponse,'data.available_type');
            const result = tmpList.map((item: string)=>({label:item,value:item}))
            setDataSourceConfig(g=>{
                g.dataSoureTypeList = result
            })
        }
    },{
        manual:true,
    })

    //获取所有可用的数据源
    const {run:runGetDataSourceList} = useRequest(async ()=>{
        const reponse:getDataSourceListInterface = await getDataSourceList()
        console.log("runGetDataSourceList",reponse);
        
    },{
        manual:true,
    })

    /**
     * 数据源类型全局只执行一次
     */
    const checkIfNeedGetDataSourceAvailableType = ()=>{
        if(dataSoureConfig.dataSoureTypeList.length == 0){
            runGetDataSourceAvailableType()
        }
        if(dataSoureConfig.dataSoureSuggestion.length == 0){
            runGetDataSourceList()
        }
    }

    return {
        dataSoureConfig,
        checkIfNeedGetDataSourceAvailableType
    }
    
}
export default useDataSourceForm;
