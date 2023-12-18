import React, { useState, useRef, useEffect } from 'react';
import { Table, Modal, ModalProps } from 'antd';
import ResizeObserver from 'rc-resize-observer';

interface ResizableTableProps {
  columns: any[];
  dataSource: any[];
  open:boolean,
  onCancel: () => void;
  loading?: boolean;
}

const ResizableTable: React.FC<ResizableTableProps> = ({ columns, dataSource,open,onCancel,loading=false }) => {
  const [tableWidth, setTableWidth] = useState<number>(0);
  const modalRef = useRef<ModalProps>(null);

  useEffect(() => {
    if (modalRef.current) {
      const modalContentWidth = modalRef.current.container?.offsetWidth || 0;
      const maxWidth = Math.min(modalContentWidth, 1000);
      setTableWidth(maxWidth);
    }
  }, [modalRef, dataSource]);

  const handleResize = ({ width }: { width: number }) => {
    const maxWidth = Math.min(width, 1000);
    setTableWidth(maxWidth);
  };

  const renderTable = () => (
    <ResizeObserver onResize={handleResize}>
      <Table
        columns={columns}
        dataSource={dataSource}
        scroll={{ x: tableWidth }}
        loading={loading}
      />
    </ResizeObserver>
  );

  return (
    <Modal
      ref={modalRef}
      open={open}
      title="数据预览"
      footer={null}
      onCancel={onCancel}
      width="auto"
      style={{ maxWidth: 1000 }}
    >
      {renderTable()}
    </Modal>
  );
};

export default ResizableTable;
