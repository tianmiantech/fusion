import { Card, Row, Col, Button, Space } from 'antd';
import {CSSProperties, ReactNode} from 'react'
import styles from './index.less'
const Index = (props: { title: any; children: any;bodyStyle?:any,extra?:ReactNode,style?:CSSProperties })=>{
    const {title,children,...rest} = props
    return <Card
    title={title}
    size="small"
    className={styles.container}
    {...rest}
    >
    {children}
    </Card>
}
export default Index;