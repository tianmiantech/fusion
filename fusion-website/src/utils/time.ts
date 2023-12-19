export const displayChineseCoastTime = (cost_time:number)=>{
    if (typeof cost_time !== 'number' || isNaN(cost_time) || cost_time < 0) {
      return '';
    }
    const milliseconds = cost_time;
    const seconds = Math.floor((cost_time / 1000) % 60);
    const minutes = Math.floor((cost_time / (1000 * 60)) % 60);
    const hours = Math.floor((cost_time / (1000 * 60 * 60)) % 24);

    let result = '';

    if (hours > 0) {
        result += hours + '小时';
    }

    if (minutes > 0) {
        result += minutes + '分钟';
    }

    if (seconds > 0 ) {
        result += seconds + '秒';
    }
    if (milliseconds > 0 && result === '') {
        result += milliseconds + '毫秒';
    }
    return result;
  }