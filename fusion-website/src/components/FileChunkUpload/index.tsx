import React, { Fragment, useRef, useEffect,useState, forwardRef,useImperativeHandle } from "react";
import  {
  Uploader,
  UploaderUnsupport,
  UploaderDrop,
  UploaderBtn,
  UploaderList,
  UploaderInterfaceRef
} from "./Components";
import { getRequestBaseURL,getToken } from '@/utils/request'
import lodash from 'lodash'
import {fileMerge,FileMergeInterface,securityScan} from './service'
import { useImmer } from "use-immer";
import { Button, Spin, message } from "antd";
import styles from './index.less'
import { useRequest,useMount } from "ahooks";
import {DataPreviewBtn} from '@/components/DataSetPreview'



interface FileChunkUploadValue {
  data_source_file:string,
  add_method:'HttpUpload'|'LocalFile'|'Database'|'',
}
interface FileChunkUploadInterface {
  uploadFinishCallBack?:Function
  value?:FileChunkUploadValue,
  onChange?: (value:FileChunkUploadValue) => void;
  disabled?:boolean
}

const Index= forwardRef((props:FileChunkUploadInterface,ref) => {

  const {uploadFinishCallBack,onChange,value={},disabled=false} = props;  
  
  const uploader = useRef<UploaderInterfaceRef>(null);
  const [uploadData,setUploadData] = useImmer({
    filename:'',
    alertMsg:'',
    alertType:'success',
    loading:false,
    showPreBtn:false,
    previewOpen:false,
    accept:'.csv, text/csv, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })  

  useEffect(()=>{
    if(value){
      //表示Form表单中主动设置值
      const source = lodash.get(value,'source','')
      if(source === 'setFieldsValue'){
        const dataSourceFile = lodash.get(value,'data_source_file','')
        uploader.current?.updateFilesAndFileList([dataSourceFile],[dataSourceFile])
        setUploadData(draft=>{
          draft.filename = dataSourceFile
          draft.showPreBtn = true
        })
      } 
    }
  },[value])

  const [fileType,setFileType] = useState('TableDataSource')
  
  const optionsConfig ={
    target:`${getRequestBaseURL()}/file/upload`,
    chunkSize:4*1024*1024,
    singleFile:true,
    simultaneousUploads:4,
    headers:{
      'X-User-Token':getToken()
    },
    processParams:(params:any)=>{
      params['upload_file_use_type'] = fileType      
      return params
    },
    processResponse: function (response:any, cb:any) {
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
        } else {
          message.error('全扫描不通过')
          clearFileList()
        }
      }
    } 
  }

  const clearFileList = ()=>{
    onChange?.({ data_source_file:'',add_method:"" });
    uploadFinishCallBack && uploadFinishCallBack([])
  }

  //获取获取预览的数据给其他的组件
  const columnsChangeCallBack = (columns:any[])=>{
    uploadFinishCallBack && uploadFinishCallBack(columns)
  }

  const renderPrewView = ()=>{
    if(uploadData.showPreBtn){
      return <DataPreviewBtn autoLoadPreView={true} columnsChangeCallBack={columnsChangeCallBack} requestParams={{data_source_file:uploadData.filename,add_method:'HttpUpload'}} />
    }
   return  null
  }
  




  return <>
    <Uploader 
      ref={uploader} 
      options={optionsConfig} 
      onFileComplete={onFileComplete}
      onFileRemoved={clearFileList}
      disabled={disabled}
      renderPrewView={renderPrewView}
      autoStart />
    </>
});
export default Index