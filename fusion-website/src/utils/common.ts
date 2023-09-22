// form rules
const formRuleRequire = (message?: string) => ({ required: true, message });
const formRuleNotChinese = { pattern: /^[^\u4e00-\u9fa5]*$/, message: '请输入非中文字符！' };
const formRuleNumber = { pattern: /^[0-9]*$/, message: '请输数字！' };

export {
  formRuleRequire,
  formRuleNotChinese,
  formRuleNumber
}