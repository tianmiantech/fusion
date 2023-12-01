import moment  from "moment";
import 'moment/locale/zh-cn';
moment.locale('zh-cn');

export async function getInitialState(): Promise<{ name: string }> {

    return { name: '@umijs/max' };
  }