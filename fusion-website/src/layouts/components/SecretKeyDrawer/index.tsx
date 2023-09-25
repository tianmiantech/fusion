

import {useState,useImperativeHandle,forwardRef} from 'react'
import { Button, Drawer,Input,Spin } from 'antd'
import { TmDrawer } from '@tianmiantech/pro';
import styles from './index.less'
import { useRequest } from 'ahooks';
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

    const resetKey = ()=>{
        setOkLoading(true);
        setTimeout(()=>{
            setOkLoading(false);
        },2000)
    }

    return <TmDrawer 
        title={'秘钥管理'} 
        onClose={()=>{
            setVisible(false)
        }} 
        width={500}
        open={visible}
        footer={null}
       >
        <Spin spinning={okLoading}>
            <span >公钥 (密钥用于与其他合作方通信时进行信息加密)</span>
            <TextArea
            readOnly
            rows={4}
            value={'AAAAB3NzaC1yc2EAAAADAQABAAABgQC60SkDe09s6ZMC2c697tP/F9XDI4GBWM09xWLIdu+c7JBwCcP6HmQu1bkCWzkFXAhCdMSxWrDa4vuTPSHy4H+C6jCPCsGtfUree+CS8At+wb+FhCDn2heaHOolLBKm0tuFQoUCFKkvdnMu+DD4hY8Yv2usQXb3C3A/xfk+KQfOpe+0gZkzNUuz5DF7G7Nz0PqbFOhCS5uhcn1IX1dsvty0jrLUXMJOCeoX22jlm2r7pgIcHvUVhwE1RJ2yy9YSQyUaQ4i86Dbmt30idCku6tUma5L6H7PH/7v3RUXxP6YcYEKGiQc3E+HrBjXrCi/paEUKdBViWM2EOmXxDnC/ZTDG/QeykCSvISkx+DD8ZrLEywT4MFfml60NTHE/Yezew9LMTzzkiE1bn7nMknsVT0bKXqn+FWhUrE/+ZwDqFAMB5OYjJy6+C28AT3jV2sHbwGwe/y2y3tuLy7XYnLdip5Svg6O+t/uXqDgS1EJf2XrYbLM7pL3eRUGL7OvnfxHjbms= '}
            />
            <div className={styles.resetBtnContainer}>
                <Button type='link' onClick={resetKey}>重置秘钥</Button>
            </div>
        </Spin>
    </TmDrawer>
})
export default Index