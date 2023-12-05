import { useEffect, useRef, useState } from 'react';
import { Modal, Input } from 'antd';

interface IProps {
  open: boolean;
  onCancel: () => void;
  onOk:(value:string) => void;
}

const RefuseModal = (props:IProps) => {
  const { open, onCancel,onOk } = props;

const [value, setValue] = useState<string>('');

const onChange = (e: any) => {
  setValue(e.target.value);
};

  return (
    <Modal
      title="拒绝原因"
      open={open}
      onCancel={onCancel}
      onOk={()=>{
        onOk && onOk(value)
      }}
    >
      <Input.TextArea
        placeholder="请输入拒绝原因"
        rows={8}
        onChange={onChange}
        value={value}
      />
    </Modal>
  )
}

export default RefuseModal;