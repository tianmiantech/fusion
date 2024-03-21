import { Button, Checkbox, Form, Input } from 'antd';
import React from 'react';
import LoginForm from './LoginForm'

import styles from './index.less'
const Index = ()=>{
        return <div className={styles.centerFlexbox}>
            <LoginForm formType={LoginForm.LOGIN_FORM_TYPE.LOGIN}/>
        </div>
       
    };
export default Index