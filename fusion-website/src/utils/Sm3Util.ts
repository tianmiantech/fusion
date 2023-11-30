import smCrypto from 'sm-crypto';

class Sm3Util {
    
  /**
   * 加密函数
   *
   * @param data 要加密的数据
   * @returns 返回加密后的字符串
   */
  static encrypt(data: string): string {
    const result = smCrypto.sm3(data);
    return result;
  }
}

export default Sm3Util;
