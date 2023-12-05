import Promoter from './components/Promoter'
import useDetail from './hooks/useDetail'
const CreatJob = ()=>{
    const {detailData,setDetailData} = useDetail();
    
    return <Promoter/>
}
export default CreatJob