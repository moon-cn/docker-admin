import {Button, Card} from 'antd';
import React from 'react';
import {get, getPageableData} from "../utils/request";
import {ProTable} from "@ant-design/pro-components";

let api = '/api/repository/';


export default class extends React.Component {






  columns = [
    {
      title: '名称',
      dataIndex: 'name',
    },
    {
      title: '镜像',
      dataIndex: 'url',
    },
    {
      title: '类型',
      dataIndex: 'type',
    },
    {
      title: '最近更新',
      dataIndex: 'modifyTime',
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, row) => {
        return <Button onClick={() => this.props.onChange(row.url)}>选择</Button>
      },
    },
  ];
  actionRef = React.createRef()

  render() {
    return <>
      <ProTable
        actionRef={this.actionRef}
        request={(params, sort) => {
          return getPageableData(api + 'list', params, sort);
        }}
        size="small"
        columns={this.columns}
        rowSelection={false}
        rowKey="name"
        search={false}
        options={{search: true}}
      />
    </>

  }
}
