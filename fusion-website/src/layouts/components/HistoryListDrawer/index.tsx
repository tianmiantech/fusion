import {useState,useImperativeHandle,forwardRef} from 'react'
import { Button, Drawer,Input,Spin } from 'antd'
import { TmDrawer } from '@tianmiantech/pro';
import History from '@/pages/home/History'
const Index = forwardRef((props,ref)=>{
    
    const [visible, setVisible] = useState(false);

    useImperativeHandle(ref,()=>{
        return {
            showDrawer:()=>{  
                setVisible(true)
            }
        }
    })

    return <TmDrawer 
        title={'历史任务'} 
        onClose={()=>{
            setVisible(false)
        }} 
        width={800}
        open={visible}
        footer={null}
       >
        <History/>
       </TmDrawer>
})
export default Index;