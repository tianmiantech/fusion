// 样本类型
export const dataResourceTypeMap = new Map([
  ['TableDataSource', '数据集'],
  ['PsiBloomFilter', '布隆过滤器'],
]);

// 样本选择方式
export const dataSetAddMethodMap = new Map([
  ['file', '选择文件'],
  ['sql', '数据库'],
]);

// 加密方式
export const encryMethodMap = new Map([
  ['MD5', 'MD5'],
  ['SHA256', 'SHA256'],
  ['none', '不哈希'],
]);