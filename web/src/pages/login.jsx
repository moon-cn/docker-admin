import React from 'react';
import {Button, Form, Input, message} from 'antd';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {setLogin, setMenu, site} from "../utils/SysConfig";
import {post} from "../utils/request";
import {history} from "umi";

export default class extends React.Component {

  state = {
    loginLoading: false
  }

  componentDidMount() {

  }

  onFinish = values => {
    this.setState({loginLoading: true})
    post('api/login', values).then(rs => {
      if (rs.success) {
        setLogin(rs.data)
        history.push('/')
      } else {
        message.error(rs.msg)
      }
      this.setState({loginLoading: false})
    }).catch(()=>{
      this.setState({loginLoading: false})
    })
  };

  render() {
    return (
      <div className='flex justify-center  w-screen h-screen'>


        <Form
          size='large'
          style={{
            width: '500px',
            marginTop: 150
          }}
          onFinish={this.onFinish}
        >
          <h1>{site.title}</h1>
          <Form.Item
            name="username"
            rules={[{required: true, message: '请输入用户名!'}]}
          >
            <Input prefix={<UserOutlined/>} placeholder="用户名"/>
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{required: true, message: '请输入密码!'}]}
          >
            <Input
              prefix={<LockOutlined/>}
              type="password"
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={this.state.loginLoading}>
              登录
            </Button>
          </Form.Item>
        </Form>

      </div>);
  }
}
