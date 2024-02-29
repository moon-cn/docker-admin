import {Button, Card, Table} from "antd";
import React from "react";
import {get, getPageableData} from "../../../utils/request";
import {ProTable} from "@ant-design/pro-components";

export default class extends React.Component {



  columns = [
    {title: '账号', dataIndex: 'username'},

    {title: '操作类型', dataIndex: 'type'},
    {title: '操作权限', dataIndex: 'permission'},
    {
      title: '时间', dataIndex: 'createTime',
      render(v) {
        return new Date(v).timeSince()
      }
    },

    {title: '消息', dataIndex: 'msg'},
    {title: '请求', dataIndex: 'request', valueType:'code'},
    {title: '响应', dataIndex: 'response', valueType:'code'},

  ]

  render() {
    return <>



      <ProTable
        search={false}

        request={(params, sort) => getPageableData( "api/oplog/list", params, sort)}
        columns={this.columns}
        rowSelection={false}
        rowKey="id"
        bordered={true}
        options={{search: false}}
        scroll={{
          x: 'max-content'
        }}


      />
    </>
  }
}
