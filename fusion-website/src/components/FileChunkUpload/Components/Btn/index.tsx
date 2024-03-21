import React, { createRef, useEffect } from "react";
import classNames from "classnames";
import "./index.css";

import { UploaderContext } from "../UploaderContext";

type UploaderBtnType = {
  directory?: boolean;
  single?: boolean;
  attrs?: Record<string, any>;
  noUploadAction?: boolean;
} & React.HTMLAttributes<HTMLLabelElement>;

export default (
  props: UploaderBtnType = { directory: false, single: false, attrs: {},noUploadAction:false}
) => {
  const btnDom = createRef<HTMLLabelElement>();
  const { getPrefixCls, uploaderRef, support } =
    React.useContext(UploaderContext);
  const { className, style, directory, single, attrs, children,noUploadAction ,...rest } = props;

  const prefixCls = getPrefixCls("btn");
  

  useEffect(() => {
    if(!noUploadAction){
      uploaderRef?.current?.assignBrowse(
        btnDom.current,
        directory,
        single,
        attrs
      );
    }
  }, []);

  return (
    <label
      className={classNames(
        prefixCls,
        {
          [`${prefixCls}-hidden`]: !support,
        },
        className
      )}
      style={style}
      ref={btnDom}
      {...rest}
    >
      {children}
    </label>
  );
};
