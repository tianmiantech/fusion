import React, {
  useState,
  useEffect,
  Fragment,
  forwardRef,
  useImperativeHandle,
  useRef,
} from "react";

import SimpleUploader from "simple-uploader.js";
import classNames from "classnames";
import UploaderList from "../List";
import UploaderDrop from "../Drop";
import UploaderBtn from "../Btn";
import UploaderUnsupport from "../Unsupport";

import UploaderContext, { defaultGetPrefixCls } from "../UploaderContext";
import "./index.css";
import { camelCase } from "../../utils";
import { Spin } from "antd";
import { useImmer } from "use-immer";
type Recordable<T = any> = Record<string, T>;

export type StatusType = {
  success: "string";
  error: "string";
  uploading: "string";
  paused: "string";
  waiting: "string";
};

export type UploaderProps = {
  options: Recordable;
  autoStart: boolean;
  fileStatusText: StatusType;
  children: (props: Recordable) => React.ReactNode;
  [key: string]: any;
  className?: string;
  style?: React.CSSProperties;
  loading?: boolean;
  accept?: string;
  acceptTitle?: string;
  disabled?: boolean;
  renderPrewView?: () => React.ReactNode;
  
};

enum UploadEventEnum {
  FILE_ADDED_EVENT = "fileAdded",
  FILES_ADDED_EVENT = "filesAdded",
  UPLOAD_START_EVENT = "uploadStart",
}

const defaultOptions = {
  //目标上传 URL，默认POST
  target: "",
  //分块大小(单位：字节)
  chunkSize: "2048000",
  //上传文件时文件内容的参数名，对应chunk里的Multipart对象名，默认对象名为file
  fileParameterName: "file",
  //失败后最多自动重试上传次数
  maxChunkRetries: 3,
  //是否开启服务器分片校验，对应GET类型同名的target URL
  testChunks: true,
};

interface UploaderDataInterface {
  fileList: any[];
  files: any[];
}

const { FILE_ADDED_EVENT, FILES_ADDED_EVENT, UPLOAD_START_EVENT } = UploadEventEnum;

const Uploader:  React.ForwardRefRenderFunction<any, UploaderProps> = (props, ref) => {
  const { className, style, options, loading=false,children,accept,disabled,acceptTitle
  ,renderPrewView } = props;

  const prefixCls = defaultGetPrefixCls("");

  const [datas, setDatas] = useImmer<UploaderDataInterface>({
    files: [],
    fileList: [],
  })

  const [started, setStarted] = useState(false);

  const uploaderRef = useRef(
    new SimpleUploader({ ...defaultOptions, ...options })
  );

  function uploadStart() {
    setStarted(true);
  }

  function fileAdded(file: any) {
    if (props[camelCase(FILE_ADDED_EVENT)]) {
      props[camelCase(FILE_ADDED_EVENT)](file);
    }
    if (file.ignored) {
      // is ignored, filter it
      return false;
    }
  }

  function filesAdded(files: Recordable, fileList: Recordable) {
    if (props[camelCase(FILES_ADDED_EVENT)]) {
      props[camelCase(FILES_ADDED_EVENT)](files, fileList);
    }
  }

  function fileRemoved(file: any) {
    updateFilesAndFileList(uploaderRef.current.files,uploaderRef.current.fileList)
  }

  function filesSubmitted(newFileList: Recordable[], newFileAndFolderList: Recordable[]) {
    updateFilesAndFileList(uploaderRef.current.files,uploaderRef.current.fileList)
    if (props.autoStart) {
      uploaderRef.current.upload();
    }
  }

  const updateFilesAndFileList = (files:any[],fileList:any[]) => {
    setDatas((draft)=>{
      //useImmer中数组为引用类型，所以需要先清空再push
      draft.files.length = 0
      draft.files.push(...files)
      draft.fileList.length = 0
      draft.fileList.push(...fileList)
    })
  }

  const eventObj: Record<string, (...args: any) => void | boolean> = {
    uploadStart,
    fileAdded,
    filesAdded,
    fileRemoved,
    filesSubmitted,
  };

  function allEvent(...args: any) {
    const name = args[0];
    const EVENTSMAP: Recordable<boolean | string> = {
      [FILE_ADDED_EVENT]: true,
      [FILES_ADDED_EVENT]: true,
      [UPLOAD_START_EVENT]: "uploadStart",
    };
    const handler = EVENTSMAP[name];
    if (handler) {
      if (handler === true) {
        return;
      }
      eventObj[handler as string](args.slice(1));
    }
    const camelCaseName = camelCase(name);
    if (props[camelCaseName]) {
      props[camelCaseName](...args.slice(1));
    }
  }

  useImperativeHandle(ref, () => ({
    getUploader: () => uploaderRef.current,
    updateFilesAndFileList: updateFilesAndFileList,
  }));



  useEffect(() => {
    if(uploaderRef.current) {
      uploaderRef.current.fileStatusText = {
        success: "上传成功",
        error: "上传失败",
        uploading: "上传中",
        paused: "暂停",
        waiting: "等待上传",
      };
  
      uploaderRef.current.on("catchAll", allEvent);
      uploaderRef.current.on(FILE_ADDED_EVENT, fileAdded);
      uploaderRef.current.on(FILES_ADDED_EVENT, filesAdded);
      uploaderRef.current.on("fileRemoved", fileRemoved);
      uploaderRef.current.on("filesSubmitted", filesSubmitted);
  
      return () => {
        uploaderRef.current.off("catchAll", allEvent);
        uploaderRef.current.off(FILE_ADDED_EVENT, fileAdded);
        uploaderRef.current.off(FILES_ADDED_EVENT, filesAdded);
        uploaderRef.current.off("fileRemoved", fileRemoved);
        uploaderRef.current.off("filesSubmitted", filesSubmitted);
        uploaderRef.current = null;
      };
    }
  
  }, []);

  useEffect(()=>{
    var fileInput = document.getElementById('fusion_job_detail_file_input') as HTMLInputElement;
    if(fileInput){
      if(disabled){
        fileInput.disabled = disabled
        }
    }
  },[disabled])
  return (
    <UploaderContext.Provider
      value={{
        getPrefixCls: defaultGetPrefixCls,
        uploaderRef,
        support: uploaderRef.current.support,
      }}
    >
      {
        <div
          className={classNames(`${prefixCls}-wrapper`, className)}
          style={style}
        >
           {children ? (
            children({ fileList:datas.fileList, files:datas.files, started })
          ) : (
          <Fragment>
            <Spin spinning={loading}>
            <UploaderUnsupport />
            <UploaderDrop>
              <p>拖拽文件上传<span style={{fontSize:12}}>{acceptTitle}</span></p>
              <UploaderBtn single={true}  attrs={{accept:accept,id:'fusion_job_detail_file_input'}}>
                上传文件
              </UploaderBtn>
              {renderPrewView && renderPrewView()}
            </UploaderDrop>
            <UploaderList fileList={datas.files} disabled={disabled}/>
            </Spin>
          </Fragment>
          )}

        </div>
      }
    </UploaderContext.Provider>
  );
};

export default forwardRef(Uploader);
