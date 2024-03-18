import Cookies from 'js-cookie';

export const setCookies = (data: object) => {
  const hosts = document.domain;
  const splitHost = hosts.split('.');
  const len = splitHost.length;
  let domainStr = '';
  if (len === 1) {
    domainStr = hosts;
  }
  if (len >= 2) {
    domainStr = `.${splitHost[len - 2]}.${splitHost[len - 1]}`;
  }
  /**
   * 如果是ip地址，直接使用ip
   */
  if(/^[0-9.]+$/.test(hosts)){
    domainStr = hosts;
  }

  if (data && typeof data === 'object') {
    for (const key in data) {
      Cookies.set('' + key, data[key] || '', { domain: domainStr });
    }
  } else {
    console.error(data);
    throw new Error('the param you input is not a object !!!');
  }
};
export const getCookie = Cookies.get;
export const getJsonCookie = Cookies.getJSON;
export const removeCookie = Cookies.remove;

export const setToken = (tokenKey: string, value: string) =>
  value && setCookies({ [tokenKey]: value });

export const getToken = Cookies.get;
export const clearToken = Cookies.remove;

export default {
  setCookies,
  getCookie,
  getJsonCookie,
  removeCookie,
  setToken,
  getToken,
  clearToken,
};
