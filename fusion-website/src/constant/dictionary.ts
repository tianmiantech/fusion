// 样本类型
export const dataResourceTypeMap = new Map([
  ['DataSet', '数据集'],
  ['BloomFilter', '布隆过滤器'],
]);

// 样本选择方式
export const dataSetAddMethodMap = new Map([
  ['file', '选择文件'],
  ['sql', '数据库'],
]);

// 加密方式
export const encryMethodMap = new Map([
  ['MD5', 'MD5'],
  ['SHA1', 'SHA1'],
  ['SHA256', 'SHA256'],
  ['none', '无'],
]);

// 数据源类型(后续改接口请求)
export const dataBaseTypeMap = new Map([
  ['Doris', 'Doris'],
  ['ClickHouse', 'ClickHouse'],
  ['MySQL', 'MySQL'],
  ['Hive', 'Hive'],
]);
