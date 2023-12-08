import React from "react";
import classNames from "classnames";
import { UploaderContext } from "../UploaderContext";
import UploaderFile from "../File";
import ReadonlyFile from '../File/ReadonlyFile'
import "./index.css";

type UploaderListType = {
  className?: string;
  style?: React.CSSProperties;
  fileList: Record<string, any>;
  children?: (props: { fileList: any }) => React.ReactNode;
};

export default (props: UploaderListType) => {
  const { className, style, fileList, children } = props;
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
            isString(file)?<ReadonlyFile file={file} list />:<UploaderFile key={file.id} file={file} list />
          ))}
        </ul>
      )}
    </div>
  );
};
