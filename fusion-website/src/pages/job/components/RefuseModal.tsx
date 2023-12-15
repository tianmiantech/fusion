import { useEffect, useRef, useState,forwardRef,useImperativeHandle } from 'react';
import { Modal, Input,message } from 'antd';
import {DisagreeJobRequestInterface,disagreeJob} from '../service'
import { useRequest } from 'ahooks';
import useDetail from "../hooks/useDetail";

interface IProps {
  
}

const RefuseModal = forwardRef((props:IProps,ref)  => {
  const [open,setOpen] = useState(false)
  const { detailData } = useDetail()


  const [value, setValue] = useState<string>('');

  const {run:runDisagreeJob,loading:disagreeJobLoading} = useRequest(async (params:DisagreeJobRequestInterface)=>{
    const reponse = await disagreeJob(params)
    const {code,data} = reponse;
    if(code === 0){
      message.success('操作成功')
    }}
    ,{ manual:true})

    const submitDisagreeJob = ()=>{
      const requestParams = {
        job_id:detailData.jobId,
        reason:value
      } as DisagreeJobRequestInterface
      runDisagreeJob(requestParams)
    }

  const onChange = (e: any) => {
    setValue(e.target.value);
  };

  useImperativeHandle(ref, () => {
    return {
      showRefuseModal:()=>{
        setOpen(true)
      }
    }
  });

  return (
    <Modal
      title="拒绝原因"
      open={open}
      onCancel={()=>{setOpen(false)}}
      onOk={()=>{
        submitDisagreeJob()
      }}
      okButtonProps={{loading:disagreeJobLoading}}
    >
      <Input.TextArea
        placeholder="请输入拒绝原因"
        rows={8}
        onChange={onChange}
        value={value}
      />
    </Modal>
  )
})

export default RefuseModal;