import lodash from 'lodash'
export const getTokenName = ():string => {
    const { host } = window.location
    const tokenName = `fusion-x-user-token-${host}`;
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
    result += `${item.method==='NONE'?'不哈希':item.method}(${item.columns.join('+')})`
    if(index !== list.length-1){
      result += '+'
    }
  })
  return result
}

export const IsEmptyObject =(obj: any): boolean=> {
  if (obj === null || obj === undefined) {
    return true;
  }
  if (typeof obj === "object") {
    for (const key in obj) {
      if (IsEmptyObject(obj[key])) {
        return true;
      }
    }
  } else if (Array.isArray(obj)) {
    for (const item of obj) {
      if (IsEmptyObject(item)) {
        return true;
      }
    }
  }

  return false;
}
  