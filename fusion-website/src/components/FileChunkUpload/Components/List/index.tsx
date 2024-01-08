import React, { useEffect } from "react";
import classNames from "classnames";
import { UploaderContext } from "../UploaderContext";
import UploaderFile from "../File";
import ReadonlyFile from '../File/ReadonlyFile'
import "./index.css";

type UploaderListType = {
  className?: string;
  style?: React.CSSProperties;
  fileList: any[]; // Change the type of fileList to any[]
  children?: (props: { fileList: any[] }) => React.ReactNode; // Change the type of fileList to any[]
  disabled?:boolean
  uploaderRef?:any
};

export default (props: UploaderListType) => {
  const { className, style, fileList, children,disabled } = props;
  const { getPrefixCls } = React.useContext(UploaderContext);

  const prefixCls = getPrefixCls("list");

  const isString = (str: any) => {
    return typeof str === 'string';
  }

  return (
    <div className={classNames(prefixCls, className)} style={style}>
      {children ? (
        children({ fileList })
      ) : (
        <ul>
          {fileList.map((file: Record<string, any>,index:number) => (
            isString(file)?<ReadonlyFile key={index} disabled={disabled} file={file} list />:<UploaderFile key={file.id} file={file} list />
          ))}
        </ul>
      )}
    </div>
  );
};
