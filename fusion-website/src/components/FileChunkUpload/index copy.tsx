import React, { useState,useMemo } from 'react';
import { Upload, Button, Modal, Progress } from 'antd';
import ChunkedUploady,{ UPLOADER_EVENTS,useRequestPreSend } from '@rpldy/chunked-uploady';

import UploadButton from "@rpldy/upload-button"
import { getBaseURL,getToken } from '@/utils/request'

const UploadButtonWithUniqueIdHeader = () => {
  useRequestPreSend((data) => {
    console.log("useRequestPreSend",data);
      return {
          options: {
              destination: {
                url:`${getBaseURL()}/file/upload`,
                headers:{'X-User-Token':getToken()},
                params:{'22':'22'}
              }
          }
      };
  });

  return <UploadButton text='选择文件'/>;
};
const FileChunkUpload: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [progress, setProgress] = useState(0);
  const [visible, setVisible] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFile(e.target.files && e.target.files[0]);
  };

  const onUploadStart = (file: File) => {
    console.log('开始上传', file);
  };

  const onUploadProgress = (bytesLoaded: number, bytesTotal: number) => {
    setProgress((bytesLoaded / bytesTotal) * 100);
  };

  const onUploadComplete = (file: File) => {
    console.log('上传完成', file);
    setProgress(0);
    setVisible(false); // 关闭Modal
  };

  const uploadOptions = {
    chunkSize: 5 * 1024 * 1024, // 设置分片大小为5MB
    retries:2,
    debug:true,
    onUploadStart,
    onUploadProgress,
    onUploadComplete,
    destination:{
        url:`${getBaseURL()}/file/upload`,
        headers:{'X-User-Token':getToken()}
    },
    params:{'11':'2'},
    // enhancer:(uploader:any)=>{
    //     uploader.registerExtension("ext-name", {
    //         foo: "bar",
    //         myMethod: () => {}        
    //     });
    //     return uploader
    // }
  };

  const listeners = useMemo(() => ({
    [UPLOADER_EVENTS.BATCH_START]: (batch:any) => {
        console.log(`Batch Start - ${batch.id} - item count = ${batch.items.length}`);
    },
    [UPLOADER_EVENTS.BATCH_FINISH]: (batch:any) => {
         console.log(`Batch Finish - ${batch.id} - item count = ${batch.items.length}`);
    },
    [UPLOADER_EVENTS.ITEM_START]: (item:any) => {
        console.log(`Item Start - ${item.id} : ${item.file.name}`);
    },
    [UPLOADER_EVENTS.ITEM_FINISH]: (item:any) => {
        console.log(`Item Finish - ${item.id} : ${item.file.name}`);
    },
}), []);

  return (
    <div>
      <ChunkedUploady  {...uploadOptions} listeners={listeners}>
        <UploadButtonWithUniqueIdHeader />
      </ChunkedUploady>
      {file && (
        <Modal title="上传进度" open={false}  onCancel={() => setVisible(false)}>
          <Progress percent={progress} />
        </Modal>
      )}
    </div>
  );
};

export default FileChunkUpload;
