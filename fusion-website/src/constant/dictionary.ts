
export const FUNSION_INITIALIZED_KEY = 'fission_initialized';
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

export const ROLE_TYPE = {
  PROMOTER: 'promoter',
  PROVIDER: 'provider',
}

// 角色类型
export const AddMethodMap = new Map([
  ['HttpUpload', 'Http上传'],
  ['LocalFile', '本地文件'],
  ['Database', '数据库'],
])

// 任务类型 
export const JOB_STATUS = {
  EDITING: 'editing',
  AUDITING: 'auditing',
  DISAGREE: 'disagree',
  WAIT_RUN: 'wait_run',
  RUNNING: 'running',
  STOP_ON_RUNNING: 'stop_on_running',
  ERROR_ON_RUNNING: 'error_on_running',
  SUCCESS: 'success',
}

// 任务状态
export const JobStatus = new Map([
  [JOB_STATUS.EDITING, '编辑中'],
  [JOB_STATUS.AUDITING, '审批中'],
  [JOB_STATUS.DISAGREE, '已拒绝'],
  [JOB_STATUS.WAIT_RUN, '等待运行'],
  [JOB_STATUS.RUNNING, '运行中'],
  [JOB_STATUS.STOP_ON_RUNNING,'终止运行'],
  [JOB_STATUS.ERROR_ON_RUNNING,'运行出错'],
  [JOB_STATUS.SUCCESS,'运行成功']
])

// 任务阶段
export const JOB_PHASE_LSIT = new Map([
  ['ConfirmMemberRole', '协商成员角色'],
  ['CreatePsiBloomFilter', '生成过滤器'],
  ['DownloadPsiBloomFilter', '下载过滤器'],
  ['Intersection', '求交'],
  ['SaveResult', '保存结果'],
])