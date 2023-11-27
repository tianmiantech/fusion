import { Button, Checkbox, Form, Input } from 'antd';
import React from 'react';
import LoginForm from '../login/LoginForm'
import styles from './index.less'
const Index = ()=>{
        return <div className={styles.centerFlexbox}>
            <LoginForm isRegister={true}/>
        </div>
       
    };
export default Index