import type { UploaderProps, StatusType } from "./Uploader";
import Uploader,{UploaderInterfaceRef} from "./Uploader";
import type { ProgressStyleType, FileType } from "./File";

export type { UploaderProps, StatusType, ProgressStyleType, FileType,UploaderInterfaceRef };

export { UploaderContext } from "./UploaderContext";

export { default as UploaderBtn } from "./Btn";
export { default as UploaderDrop } from "./Drop";
export { default as ChunkUploadFile } from "./File";
export { default as UploaderFiles } from "./Files";
export { default as UploaderList } from "./List";
export { default as UploaderFile } from "./File";
export { default as UploaderUnsupport } from "./Unsupport";

export { Uploader };

export default Uploader;
