import { useEffect, useRef, useState } from 'react';
import { Drawer } from 'antd';
import { TmTable } from '@tianmiantech/pro';

interface IProps {
  open: boolean;
  onClose: () => void;
}

const BloomFilterManage = (props:IProps) => {
  const { open, onClose } = props;

  const tableRef = useRef();
  const [tableData, setTableData] = useState([{
    id: 1,
    name: 'test',
    count: 10000,
    hash: 'MD5(id)'
  }, {
    id: 2,
    name: 'test1',
    count: 10000,
    hash: 'MD5(id)'
  }, {
    id: 3,
    name: 'test2',
    count: 10000,
    hash: 'MD5(id)'
  }]);

  const columns = [{
    key: 'name',
    dataIndex: 'name',
    title: '文件名',
  }, {
    key: 'count',
    dataIndex: 'count',
    title: '数据量',
  }, {
    key: 'hash',
    dataIndex: 'hash',
    title: '主键',
  }, {
    key: 'option',
    dataIndex: 'option',
    title: '操作',
    width: 60,
    render: (_: any, record: any) => 
      (<a key="edit">删除</a>),
  }]

  return (
    <Drawer
      title="布隆过滤器管理"
      placement="right"
      open={open}
      onClose={onClose}
      width="50%"
    >
      <TmTable
        ref={tableRef}
        columns={columns}
        dataSource={tableData}
        rowKey="id"
        pagination={{
          pageSize: 15,
        }}
      >
        <TmTable.Table />
      </TmTable>
    </Drawer>
  )
}

export default BloomFilterManage;
