import axios from 'axios';

export const codeMessage = {
  200: '服务器成功返回请求的数据。',
  201: '新建或修改数据成功。',
  202: '一个请求已经进入后台排队（异步任务）。',
  204: '删除数据成功。',
  400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  401: '用户没有权限（令牌、用户名、密码错误）。',
  403: '用户得到授权，但是访问是被禁止的。',
  404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  406: '请求的格式不可得。',
  410: '请求的资源被永久删除，且不会再得到的。',
  422: '当创建一个对象时，发生一个验证错误。',
  500: '服务器发生错误，请检查服务器。',
  502: '网关错误。',
  503: '服务不可用，服务器暂时过载或维护。',
  504: '网关超时。',
}

export interface IRequest {
  baseURL: string;
  timeout?: number;
  skipErrorMessage?: boolean;
  successCode?: string | number;
  getHeaders?: Function;
  message: { error: Function; success: Function; info: Function };
  invalidTokenCodes: string[];
  onTokenInvalid: Function;
  type?: string;
  /**
   * 使用info弹窗提示的code列表
   */
  infoCodes: string[];
  whiteList: string[];
}

const createRequest = ({
  baseURL,
  timeout = 30000,
  skipErrorMessage = false,
  successCode = '0',
  getHeaders = () => ({}),
  message,
  infoCodes = [],
  invalidTokenCodes = [],
  onTokenInvalid = () => { },
  type = 'iam',
  whiteList = []
}: IRequest) => {
  const instance = axios.create({
    timeout,
    baseURL,
  });
  instance.interceptors.request.use(
    (config: any) => {
      Object.assign(config.headers||{}, getHeaders());
      config.baseURL = baseURL;
      return config;
    },
    (error) => {
      console.error(error);
      message.error("请求出错");
    },
  );

  instance.interceptors.response.use(
    (response:any) => {
      const { parseResponse = true, skipErrorMessage, skipAllError, url } = response.config;
      const { code, message: msg } = response.data;
      // 跳过所有错误，自行处理
      if (skipAllError) {
        return parseResponse ? response.data : response;
      } else if (whiteList.includes(String(url))) {
        return parseResponse ? response.data : response;
      } else if (invalidTokenCodes.some((each) => String(each) === String(code))) {
        onTokenInvalid(response);
      } else if (infoCodes.some((each) => String(each) === String(code))) {
        message.info(msg)
      } else if (!skipErrorMessage && code !== successCode) {
        message.error(msg);
      }
      return parseResponse ? response.data : response;
    },
    (error) => {
      const { response } = error;
      message.error(response.data?.message || codeMessage[response.status as keyof typeof codeMessage]);
      return response;
    },
  );

  return {
    ...instance,
    get: (url: string, params?: object, rest?: any) => {
      const getParams = { ...params }
      return instance.get(url, { params: getParams, ...rest })
    },
  };
};

export default createRequest;
