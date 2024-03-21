import { Drawer, Button } from 'antd';
import styles from  './index.less';
const TmDrawer = ({
  title,
  children,
  footer,
  okLoading,
  onClose,
  onOk,
  okText = '确定',
  cancelText = '取消',
  className: propClassName,
  ...restProps
}: {
  title?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  okLoading?: boolean;
  onClose: () => void;
  onOk?: () => void;
  okText?: string;
  cancelText?: string;
  className?: string;
  [key: string]: any;
}) => {
  const footerNode =
    footer === undefined ? (
      <div style={{ float: 'right', display: 'flex', columnGap: 10 }}>
        <Button onClick={onClose}>{cancelText}</Button>
        <Button type="primary" onClick={onOk} loading={okLoading}>
          {okText}
        </Button>
      </div>
    ) : (
      footer
    );
  return (
    <Drawer
      className={`${styles.tmDrawerStyle} ${propClassName || ''}`}
      closable={false}
      title={title}
      footer={footerNode}
      onClose={onClose}
      {...restProps}
    >
      <div>{children}</div>
    </Drawer>
  );
};

export default TmDrawer;
