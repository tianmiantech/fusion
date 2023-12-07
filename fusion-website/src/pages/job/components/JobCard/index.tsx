import { Card, Row, Col, Button, Space } from 'antd';

const Index = (props: { title: any; children: any;bodyStyle?:any })=>{
    const {title,children,...rest} = props
    const cardStyles = {
        headStyle: {
          height: 50,
          lineHeight: 2
        },
        bodyStyle: {
          OverflowY: 'hidden',
          Position: 'relative'
        }
      }
    return <Card
    title={title}
    size="small"
    {...cardStyles}
    {...rest}
    >
    {children}
    </Card>
}
export default Index;