import lodash from 'lodash'
export const getTokenName = ():string => {
    const { host } = window.location
    const tokenName = `fusion-x-user-token-${host}-${process.env.BASE_PATH}`;
    return tokenName;
  };



interface hashConfigItemInterface {
  list:{
    method:string,
    columns:string[]
  }[]
}

export const renderHashConfig = (hash_config:hashConfigItemInterface) => {
  const list = lodash.get(hash_config,'list',[])
  let result = ''
  list.forEach((item:any,index:number)=>{
    const columns = item.columns || []; // 处理columns可能为空的情况
    result += `${item.method==='NONE'?'不哈希':item.method}(${columns.join('+')})`
    if(index !== list.length-1){
      result += '+'
    }
  })
  return result
}

export const IsEmptyObject =(obj: any): boolean=> {
  if (obj === null || obj === undefined || obj === "" ||  (obj+'').trim() === "[]") {
    return true;
  }
  if (Array.isArray(obj)) {
    if(obj.length === 0){
      return true;
    }
    for (const item of obj) {
      if (IsEmptyObject(item)) {
        return true;
      }
    }
  } else  if (typeof obj === "object") {
    for (const key in obj) {
      if (IsEmptyObject(obj[key])) {
        return true;
      }
    }
  }
  return false;
}

/**
 * 根据浏览器的地址解析出react-router的地址
 * @param redirectParam 
 */
export const getReactRouter = (redirectParam:string) => {
  const BASE_PATH = process.env.BASE_PATH;
  if(redirectParam){
    const tmPArray = redirectParam.split(BASE_PATH||'');
    if(tmPArray.length>1){
      return tmPArray[1];
    }
  }
  return redirectParam;
}
  