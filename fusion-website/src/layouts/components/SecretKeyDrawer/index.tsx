

import {useState,useImperativeHandle,forwardRef} from 'react'
import { Drawer,Input } from 'antd'
import { TmDrawer } from '@tianmiantech/pro';
import styles from './index.less'
const { TextArea } = Input;
const Index = forwardRef((props,ref)=>{
    const [visible, setVisible] = useState(false);
    const [okLoading,setOkLoading] = useState(false);

    useImperativeHandle(ref,()=>{
        return {
            showDrawer:()=>{  
                setVisible(true)
            }
        }
    })

    return <TmDrawer 
        title={'秘钥管理'} 
        onClose={()=>{
            setVisible(false)
        }} 
        width={500}
        open={visible}
        footer={null}
       >
        <span >公钥 (密钥用于与其他合作方通信时进行信息加密)</span>
        <TextArea
         readOnly
         rows={4}
        />
    </TmDrawer>
})
export default Index