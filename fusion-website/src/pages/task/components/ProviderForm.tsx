import React, { useState } from 'react';
import { Form, Input, Button, Row, Col } from 'antd';

const ProviderForm = () => {
  const [formRef] = Form.useForm();

  return (
    <>
      <Row justify="center" className="form-scroll">
        <Col lg={{span: 16}} md={{span: 24}}>
          <Form
            form={formRef}
            layout="vertical"
          >
            <Form.Item name="name" label="协作方名称">
              <Input placeholder='请输入' />
            </Form.Item>
            <Form.Item name="host" label="协作方Host">
              <Input placeholder='请输入' />
            </Form.Item>
            <Form.Item name="host" label="端口">
              <Input placeholder='请输入' />
            </Form.Item>
            <Form.Item name="publicKey" label="公钥">
              <Input.TextArea placeholder='请输入' rows={4} />
            </Form.Item>
            <Form.Item>
              <Row justify="end">
                <Button type="link">连通性测试</Button>
              </Row>
            </Form.Item>
          </Form>
        </Col>
      </Row>
    </>
  );
};

export default ProviderForm;
