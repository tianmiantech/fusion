import { Card, Row, Col, Button, Space } from 'antd';

const Index = (props: { title: any; children: any; })=>{
    const {title,children} = props
    const cardStyles = {
        headStyle: {
          height: 50,
          lineHeight: 2
        },
        bodyStyle: {
          height: 'calc(100vh - 92px)',
          OverflowY: 'hidden',
          Position: 'relative'
        }
      }
    return <Card
    title={title}
    size="small"
    {...cardStyles}
    >
    {children}
    </Card>
}
export default Index;