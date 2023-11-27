import React, { Fragment, useRef, useEffect,useState } from "react";
import {
  Uploader,
  UploaderUnsupport,
  UploaderDrop,
  UploaderBtn,
  UploaderList,
} from "react-simple-uploader";
import { getBaseURL,getToken } from '@/utils/request'
import lodash from 'lodash'
import {fileMerge,FileMergeInterface} from './service'
interface UploaderInterface {
  fileList:Record<string, any>;
}
const Index= () => {
  const uploader = useRef(null);

  const [fileType,setFileType] = useState('PsiBloomFilter')

  useEffect(() => {
    console.log('uploader',uploader.current?.getUploader());
  }, []);

  
  const optionsConfig ={
    target:`http://172.31.21.36:8080/fusion/file/upload`,
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
    const response = await fileMerge(reqData);
    console.log("onFileComplete",response);
    
  }


  return (
    <Uploader 
      ref={uploader} 
      options={optionsConfig} 
      onFileComplete={onFileComplete}
      autoStart >
      {(fileObj:UploaderInterface) => {
        const {fileList} = fileObj
       return  (
          <Fragment>
            <UploaderUnsupport />
            <UploaderDrop>
              <p>拖拽文件上传</p>
              <UploaderBtn>上传文件</UploaderBtn>
              {/* <UploaderBtn directory>上传文件夹</UploaderBtn> */}
            </UploaderDrop>
            <UploaderList fileList={fileList} />
          </Fragment>
        )
      }}
    </Uploader>
  );
};
export default Index