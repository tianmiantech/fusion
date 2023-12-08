import { useRef,useImperativeHandle,useEffect, forwardRef } from "react";
import {preViewData,PeviewDataRequestInterface} from './service'
import { useRequest,useMount } from "ahooks";
import ResizableTable from './ResizableTable';
import { useImmer } from "use-immer";
import {UploaderBtn} from '../FileChunkUpload/Components'
import styles from './index.less'

interface UploadDataPropsInterface {
  filename:string,
  autoLoadPreView?:boolean,//是否主动话加载预览
  columnsChangeCallBack?:(columns:any[])=>void,//当加载到数据后，如果其他的组件需要使用预览数据，可以调用这个方法
}
const HttpUpload = forwardRef((props:UploadDataPropsInterface,ref) => {
  const {filename ,columnsChangeCallBack,autoLoadPreView=false} = props
  const [httpData,setHttpData] = useImmer({
    previewOpen:false,
    columns:[],
    dataSource:[],
  })

  const getPreViewData = (filename:string)=>{
    const params = {
      data_source_file:filename,
      add_method:'HttpUpload'
    } as PeviewDataRequestInterface
    runPreViewData(params)
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
    if(httpData.columns.length == 0 && filename){
      await getPreViewData(filename)
    } 
    setHttpData(draft=>{draft.previewOpen = true})
  }

  useEffect(()=>{
  if(autoLoadPreView && filename){
    getPreViewData(filename)
  }
  },[autoLoadPreView,filename])

  useEffect(()=>{
    if(httpData.columns.length>0){
      columnsChangeCallBack && columnsChangeCallBack(httpData.columns)
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
    />
  </>
})
export default HttpUpload
