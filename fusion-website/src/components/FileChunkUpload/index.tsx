import React, { Fragment, useRef, useEffect,useState, forwardRef,useImperativeHandle } from "react";
import  {
  Uploader,
  UploaderUnsupport,
  UploaderDrop,
  UploaderBtn,
  UploaderList,
  UploaderInterfaceRef
} from "./Components";
import { getBaseURL,getToken } from '@/utils/request'
import lodash from 'lodash'
import {fileMerge,FileMergeInterface,securityScan,PeviewDataRequestInterface,preViewData} from './service'
import { useImmer } from "use-immer";
import { Button, Spin, message } from "antd";
import styles from './index.less'
import { useRequest } from "ahooks";
import DataSetPreview from "../DataSetPreview";
interface UploaderInterface {
  fileList:Record<string, any>;
}

interface FileChunkUploadValue {
  data_source_file:string,
  add_method:'HttpUpload'|'LocalFile'|'Database',
}
interface FileChunkUploadInterface {
  uploadFinishCallBack?:Function
  value?:FileChunkUploadValue,
  onChange?: (value:FileChunkUploadValue) => void;
  disabled?:boolean
}

const Index= forwardRef((props:FileChunkUploadInterface,ref) => {

  const {uploadFinishCallBack,onChange,value={},disabled} = props;
  
  const uploader = useRef<UploaderInterfaceRef>(null);
  const [uploadData,setUploadData] = useImmer({
    filename:'',
    alertMsg:'',
    alertType:'success',
    loading:false,
    showPreBtn:false,
    previewOpen:false,
    preViewData:{
      columns:[],
      dataSource:[],
    }
  })

  const [fileType,setFileType] = useState('TableDataSource')


  
  const optionsConfig ={
    target:`${getBaseURL()}/file/upload`,
    chunkSize:4*1024*1024,
    simultaneousUploads:4,
    headers:{
      'X-User-Token':getToken()
    },
    processParams:(params:any)=>{
      params['upload_file_use_type'] = fileType      
      return params
    },
    processResponse: function (response:any, cb:any) {
      console.log("response",response);
      // const responseObj = JSON.parse(response||'{}')
      // const code = lodash.get(responseObj,'code')
      cb(null, response)
    },
  }
  const onFileComplete = async (rootFile:any)=>{
    const reqData = {
      filename:rootFile.name,
      identifier:rootFile.uniqueIdentifier,
      upload_file_use_type:fileType
    } as FileMergeInterface
    setUploadData(draft=>{draft.loading = true})
    const response = await fileMerge(reqData);
    const {code,data} = response
    if (code == 0) {
      const {filename,scan_session_id} = data
      setUploadData(draft=>{
        draft.filename = filename
      })
      fileSecurityScan(scan_session_id,filename)
    } else {
      message.error('文件合并失败')
      clearFileList()
      setUploadData(draft=>{draft.loading = false})
    }
  }

    // 预览接口请求
  const {run:runPreViewData,loading } = useRequest(async (params:PeviewDataRequestInterface)=>{
    const reponse = await preViewData(params)
    const {code,data} = reponse
    if(code == 0) {
      setUploadData(draft=>{
        const columns = data.header.map((item: string)=>{
          return {
              title:item,
              dataIndex:item,
          }
        }) 
        draft.preViewData = {
          columns,
          dataSource:data.rows
        }
      })
    } 
  },{manual:true})

  // 上传完成后，文件安全扫描
  let  fileSecurityScanTimer: NodeJS.Timeout;
  const fileSecurityScan = async (scan_session_id:string,filename:string)=>{
    const response = await securityScan(scan_session_id);
    setUploadData(draft=>{draft.loading = false})
    const {code,data} = response
    if (code == 0) {
      const finished = lodash.get(data,'scan_result.finished');
      const success = lodash.get(data,'scan_result.success');
      if(!finished){
        fileSecurityScanTimer = setTimeout(() => {
          fileSecurityScan(scan_session_id,filename);
        }, 3000);
      } else {
        clearTimeout(fileSecurityScanTimer);
        if(success) {
          message.success('全扫描完成')
          setUploadData(draft=>{
            draft.showPreBtn = true
          })      
          onChange?.({ data_source_file:filename,add_method:'HttpUpload' });
          getPreViewData(filename)
        } else {
          message.error('全扫描不通过')
          clearFileList()
        }
      }
    } 
  }

  const clearFileList = ()=>{
    uploader.current?.setFileList([])
  }

  const getPreViewData = (filename:string)=>{
    const params = {
      data_source_file:filename,
      add_method:'HttpUpload'
    } as PeviewDataRequestInterface
    runPreViewData(params)
  }

  const onPreViewClick = async ()=>{
    if(uploadData.preViewData.columns.length == 0 && uploadData.filename){
      await getPreViewData(uploadData.filename)
    } 
    setUploadData(draft=>{draft.previewOpen = true})
  }

  useEffect(()=>{
    const columns = uploadData.preViewData.columns.map(item=>{
      return lodash.get(item,'dataIndex')
    })
    uploadFinishCallBack && uploadFinishCallBack({
      uploadFileName:uploadData.filename,
      dataourceColumnList:columns
    })
  },[uploadData.filename,uploadData.preViewData.columns])
  

  return <>
    <Uploader 
      ref={uploader} 
      options={optionsConfig} 
      onFileComplete={onFileComplete}
      autoStart >
      {(fileObj:UploaderInterface) => {
        const {fileList} = fileObj
       return  (
          <Fragment>
            <Spin spinning={uploadData.loading|| loading}>
              <UploaderUnsupport />
              <UploaderDrop>
                <p>拖拽文件上传</p>
                <UploaderBtn single={true} attrs={{accept:'.csv, text/csv, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'}}>
                  上传文件
                </UploaderBtn>
                {
                  uploadData.showPreBtn && <UploaderBtn 
                  className={styles.preBtn} 
                  noUploadAction={true}
                  onClick={()=>{onPreViewClick()}}
                  >
                  预览
                </UploaderBtn>
                }
                
                {/* <UploaderBtn directory>上传文件夹</UploaderBtn> */}
              </UploaderDrop>
              <UploaderList fileList={fileList} />
            </Spin>
          </Fragment>
        )
      }}
    </Uploader>
    <DataSetPreview
        open={uploadData.previewOpen}
        onCancel={() => {setUploadData(draft=>{draft.previewOpen = false})}}
        columns={uploadData.preViewData.columns}
        dataSource={uploadData.preViewData.dataSource}
      />
    </>
});
export default Index