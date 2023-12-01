// 样本类型
export const dataResourceTypeMap = new Map([
  ['TableDataSource', '数据集'],
  ['PsiBloomFilter', '布隆过滤器'],
]);

// 样本选择方式
export const dataSetAddMethodMap = new Map([
  ['HttpUpload', '选择文件'],
  ['Database', '数据库'],
]);

// 加密方式
export const encryMethodMap = new Map([
  ['MD5', 'MD5'],
  ['SHA256', 'SHA256'],
  ['none', '不哈希'],
]);

// 角色类型
export const RoleMap = new Map([
  ['promoter', '发起方'],
  ['provider', '协作方'],
])

// 角色类型
export const AddMethodMap = new Map([
  ['HttpUpload', 'Http上传'],
  ['LocalFile', '本地文件'],
  ['Database', '数据库'],
])

// 任务状态
export const JobStatus = new Map([
  ['editing', '编辑中'],
  ['disagree', '已拒绝'],
  ['wait_run', '等待运行'],
  ['running', '运行中'],
  ['stop_on_running','终止运行'],
  ['error_on_running','运行出错'],
  ['success','运行成功']
])