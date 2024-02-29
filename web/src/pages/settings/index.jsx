import React from "react";
import {Card, Tabs} from "antd";
import {BankOutlined, CreditCardOutlined, FileOutlined, UserOutlined} from "@ant-design/icons";
import User from "./user";
import Tenant from "./tenant";
import GitCredential from "./gitCredential";
import Oplog from "./oplog";


export default class extends React.Component{

  render() {


    const items = [

      {
        key: 'gitCredential',
        label: <><CreditCardOutlined/>GIT凭据</>,
        children: <GitCredential/>
      },
      {
        key: 'tenant',
        label: <><BankOutlined />租户</>,
        children: <Tenant/>
      },
      {
        key: 'user',
        label: <><UserOutlined />用户</>,
        children: <User/>
      },

      {
        key: 'oplog',
        label: <><FileOutlined />操作日志</>,
        children: <Oplog/>
      },
    ];
    return <Card> <Tabs items={items} /></Card>;

  }
}
