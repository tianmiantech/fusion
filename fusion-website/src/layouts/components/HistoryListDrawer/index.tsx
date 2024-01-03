import {useState,useImperativeHandle,forwardRef} from 'react'
import { Button, Drawer,Input,Spin } from 'antd'
import { TmDrawer } from '@tianmiantech/pro';
import JobList from '@/pages/home/JobList'
const Index = forwardRef((props,ref)=>{
    
    const [visible, setVisible] = useState(false);

    useImperativeHandle(ref,()=>{
        return {
            showDrawer:()=>{  
                setVisible(true)
            }
        }
    })

    const handleCallBack = ()=>{
        setVisible(false)
    }

    return <TmDrawer 
        title={'历史任务'} 
        onClose={()=>{
            setVisible(false)
        }} 
        width={800}
        open={visible}
        footer={null}
       >
        <JobList handleCallBack={handleCallBack}/>
       </TmDrawer>
})
export default Index;