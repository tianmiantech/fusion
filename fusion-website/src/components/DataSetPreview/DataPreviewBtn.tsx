import { useRef,useImperativeHandle,useEffect, forwardRef } from "react";
import {preViewData,PeviewDataRequestInterface} from './service'
import { useRequest,useMount } from "ahooks";
import ResizableTable from './ResizableTable';
import { useImmer } from "use-immer";
import {UploaderBtn} from '../FileChunkUpload/Components'
import styles from './index.less'

interface UploadDataPropsInterface {
  requestParams:PeviewDataRequestInterface,
  autoLoadPreView?:boolean,//是否主动话加载预览
  columnsChangeCallBack?:(columns:any[])=>void,//当加载到数据后，如果其他的组件需要使用预览数据，可以调用这个方法
}
const DataPreviewBtn = forwardRef((props:UploadDataPropsInterface,ref) => {
  const {requestParams ,columnsChangeCallBack,autoLoadPreView=false} = props
  const [httpData,setHttpData] = useImmer({
    previewOpen:false,
    columns:[],
    dataSource:[],
  })

  const getPreViewData = ()=>{
    runPreViewData(requestParams)
  }


     // 预览接口请求
  const {run:runPreViewData,loading } = useRequest(async (params:PeviewDataRequestInterface)=>{
    const reponse = await preViewData(params)
    const {code,data} = reponse
    if(code == 0) {
      setHttpData(draft=>{
        const columns = data.header.map((item: string)=>{
          return {
              title:item,
              dataIndex:item,
          }
        }) 
        draft.columns = columns
        draft.dataSource = data.rows
      })
    } 
  },{manual:true})

  const onPreViewClick = async ()=>{
    if(httpData.columns.length == 0 && (requestParams.data_source_file || requestParams.sql)){
      await getPreViewData()
    } 
    setHttpData(draft=>{draft.previewOpen = true})
  }

  useEffect(()=>{
  if(autoLoadPreView && (requestParams.data_source_file || requestParams.sql)){

    getPreViewData()
  }
  },[autoLoadPreView,requestParams])


  useEffect(()=>{
    if(httpData.columns.length>0){
      const tmpResult = httpData.columns.map((item:any)=>item.dataIndex)
      columnsChangeCallBack && columnsChangeCallBack(tmpResult)
    }
  },[httpData.columns])

  return <>
    <UploaderBtn 
      className={styles.preBtn} 
      noUploadAction={true}
      onClick={()=>{onPreViewClick()}}
      >
      预览数据
    </UploaderBtn>
    <ResizableTable
        open={httpData.previewOpen}
        onCancel={() => {setHttpData(draft=>{draft.previewOpen = false})}}
        columns={httpData.columns}
        dataSource={httpData.dataSource}
        loading={loading}
    />
  </>
})
export default DataPreviewBtn
