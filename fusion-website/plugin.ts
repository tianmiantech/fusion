import { IApi } from 'umi';
export default (api: IApi) => {
    api.addHTMLStyles(() => [`body { margin: 0; }`]);
  };