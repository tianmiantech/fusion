
import {useEffect} from 'react';
import {Card,Alert} from 'antd';
import QRCode from 'qrcode';
// import Jimp from 'jimp/es';
import { useImmer } from 'use-immer';
import { IsEmptyObject } from '@/utils/utils';
interface QRCodeCardProps {
    configData: any;
}

const QRCodeCard: React.FC<QRCodeCardProps> = (props:QRCodeCardProps) => {
    const {configData} = props;
    const [datas,setDatas] = useImmer({
        text:'测试1',
        qrCodeUrl:'',
        qrCodeJson:{}
    })

    useEffect(()=>{
        if(!IsEmptyObject(configData)){
            setDatas((draft)=>{
                draft.qrCodeJson = configData;
            })
            console.log('configData',configData);
            
            generateQRCodeWithText(configData, '测试1').then((res)=>{
                console.log('res',res);
                
                setDatas((draft)=>{
                    draft.qrCodeUrl = res;
                    console.log('qrCodeUrl',res);
                    
                })
            })
        }
    },[configData])

    // 封装 JSON 成带有文本信息的二维码图片并返回图片 URL
    const generateQRCodeWithText = async (json: QRCodeCardProps, text1: string): Promise<string>=> {
        try {
        const jsonString = JSON.stringify(json);
        const qrCodeDataUrl = await QRCode.toDataURL(jsonString);
        return qrCodeDataUrl;
        } catch (error) {
            console.error('生成带有文本信息的二维码失败:', error);
            throw error;
        }
    }



    return <Card title='配置信息二维码'>
        <Alert message="此二维码根据上面的配置信息生成，可将此二维码拍照或者下载发送给合作方，合作方扫描此二维码可向我方发送任务" type="info" />
        <div style={{textAlign:'center',marginTop:20}}>
            <img src={datas.qrCodeUrl} alt="" style={{width:200}}/>
        </div>
    </Card>
};
export default QRCodeCard;