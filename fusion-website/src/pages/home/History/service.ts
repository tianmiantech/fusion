import { request } from "@/utils/request";
interface historyListPro {

}
export const getHistoryList = ()=>{
    return request.get('/api/history')
}
