import { useRequest,useMount } from 'ahooks';
import { getDataSourceAvailableType,getDataSourceList,testDataSource,TestDataSourceInterface } from '../service'
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

interface testDataSourceReponseInterface {
    code:string,
    success:boolean
}

const useDataSourceForm = ()=>{

    const [dataSoureConfig,setDataSourceConfig] = useImmer<dataSoureConfigInterface>({
        dataSoureTypeList:[],
        dataSoureSuggestion:[]
    })
    //获取数据库类型
    const getDataSourceAvailableTypeHandeler = async():Promise<null>=> {
        const reponse:getDataSourceAvailableTypeInterface = await getDataSourceAvailableType()
        const {code} = reponse;
        if(code === 0){
            const tmpList = lodash.get(reponse,'data.available_type');
            const result = tmpList.map((item: string)=>({label:item,value:item}))
            setDataSourceConfig(g=>{
                g.dataSoureTypeList = result
            })
        }
        return null
    }

    const {run:runGetDataSourceAvailableType} = useRequest(getDataSourceAvailableTypeHandeler,{
        manual:true,
    })

    //获取所有可用的数据源
    const getDataSourceListHandeler =  async():Promise<null>=> {
        const reponse:getDataSourceListInterface = await getDataSourceList()
        return null
    }

    const {run:runGetDataSourceList} = useRequest(getDataSourceListHandeler,{
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

    //测试数据源是否可以用
    const testDataSourceHandeler =  async(params:TestDataSourceInterface):Promise<testDataSourceReponseInterface>=> {
        const reponse:testDataSourceReponseInterface = await testDataSource(params)
        console.log("reponse",reponse);
        const {code,success} = reponse
        return {code,success}
    }

    const {run:runTestDataSource,data:testDataSourceCallBakData} = useRequest(testDataSourceHandeler,{
        manual:true,
    })
    


    return {
        dataSoureConfig,
        checkIfNeedGetDataSourceAvailableType,
        runTestDataSource,
        testDataSourceCallBakData
    }
    
}
export default useDataSourceForm;
