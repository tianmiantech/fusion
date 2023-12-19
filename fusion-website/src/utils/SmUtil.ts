import smCrypto from 'sm-crypto';

//为0时在加密的串前加上04,后端才能解密成功,同样后端加密后也会返回带04的加密串给前端,cipherMode为1的话必须去掉04才能解密成功
const cipherMode = 0; // 选择加密策略，1 - C1C3C2，0 - C1C2C3，默认为1
class SmUtil {
    
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

  /**
 * 使用公钥加密数据
 *
 * @param data - 待加密的数据
 * @param publicKey - 公钥
 * @returns 加密后的数据
 */
  static encryptByPublicKey(publicKey: string,data: string): string {
    return '04' + smCrypto.sm2.doEncrypt(data, publicKey, cipherMode);
  }
}

export default SmUtil;
