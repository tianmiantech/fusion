import classNames from "classnames";
import React, { Fragment, useEffect, useRef, useState } from "react";
import Uploader from "simple-uploader.js";
import { UploaderContext } from "../UploaderContext";
import { secondsToStr } from "../../utils";
import events from "./file-events";
import "./index.css";

export type ProgressStyleType = {
  progress?: string;
  WebkitTransform?: string;
  MozTransform?: string;
  msTransform?: string;
  transform?: string;
};

type Recordable<T = any> = Record<string, T>;

export type FileType = {
  style?: React.CSSProperties;
  className?: string;
  file: any;
  list: boolean;
  children?: (props: Recordable) => React.ReactNode;
  setFileList: (fileList: any) => void;
  fileList:any[]
  fileIndex:number
};

export default (props: FileType) => {
  const { className, style, file, list = true, children,setFileList,fileIndex,fileList } = props;
  const { getPrefixCls } = React.useContext(UploaderContext);

  const prefixCls = getPrefixCls("file");

  let handlers: Recordable = {};
  const tid = useRef<any>(null);

  const [iconType, setIconType] = useState("");

  const [progressStyle, setProgressStyle] = useState<ProgressStyleType>({});
 

  function getFileExtension(filename: string) {
    // 如果文件名为空或者不包含点号，返回空字符串
    if (!filename || filename.indexOf('.') === -1) {
      return '';
    }
  
    // 使用正则表达式获取最后一个点号之后的部分作为后缀名
    const tmpName = filename.split('.').pop()||'';
    return tmpName.toLowerCase(); // 将后缀名转换为小写（可选）
  }

  function fileCategory() {
    let type = "unknown";
    const tmp = getFileExtension(file);
    const typeMap =  {
      image: ["gif", "jpg", "jpeg", "png", "bmp", "webp"],
      video: ["mp4", "m3u8", "rmvb", "avi", "swf", "3gp", "mkv", "flv"],
      audio: ["mp3", "wav", "wma", "ogg", "aac", "flac"],
      document: [
        "doc",
        "txt",
        "docx",
        "pages",
        "epub",
        "pdf",
        "numbers",
        "csv",
        "xls",
        "xlsx",
        "keynote",
        "ppt",
        "pptx",
      ],
    };
    for (const type_key in typeMap) {
      if (typeMap[type_key as keyof typeof typeMap].includes(tmp)) {
        type = type_key;
      }
    }
    setIconType(type);
  }
  useEffect(() => {
    fileCategory();
  }, [file]);


  const removeFile = () => {
    setFileList([])
  }



  return (
    <div className={classNames(prefixCls, status, className)} style={style}>
      {children ? (
        children({
          file,
          list,
          status,
          progressStyle,
          fileCategory,
        })
      ) : (
        <Fragment>
          <div
            className={classNames(`${prefixCls}-progress`)}
            style={{ ...progressStyle }}
          />
          <div className={`${prefixCls}-info`}>
            <div className={`${prefixCls}-name`}>
              <i className={classNames(`${prefixCls}-icon`, iconType)} />
              {file || ""}
            </div>
            <div className={`${prefixCls}-size`}/>
            <div className={`${prefixCls}-meta`} />
            <div className={`${prefixCls}-status`}/>
            <div className={`${prefixCls}-actions`}>
              <span className={`${prefixCls}-remove`} onClick={()=>{removeFile()}} />
            </div>
          </div>
        </Fragment>
      )}
    </div>
  );
};
