import { useEffect, useRef, useState } from 'react';
import { Modal, Input } from 'antd';

interface IProps {
  open: boolean;
  onCancel: () => void;
}

const RefuseModal = (props:IProps) => {
  const { open, onCancel } = props;

  return (
    <Modal
      title="拒绝原因"
      open={open}
      onCancel={onCancel}
    >
      <Input.TextArea
        placeholder="请输入拒绝原因"
        rows={8}
      />
    </Modal>
  )
}

export default RefuseModal;