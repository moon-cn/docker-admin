import {Button} from 'antd';
import React from 'react';


import {ProTable} from "@ant-design/pro-components";
import {getPageableData} from "../utils/request";

let api = '/api/repository/';


export default class extends React.Component {

  columns = [
    {
      title: '镜像',
      dataIndex: 'url',
    },
    {
      title: '镜像Tag',
      dataIndex: 'name',
    },

    {
      title: '更新时间',
      dataIndex: 'time',
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, row) => {
        return <Button onClick={() => this.props.onChange(row.name)}>选择</Button>
      },
    },
  ];


  render() {
    return <ProTable
      request={(params, sort) => {
        params.url = this.props.url
        return getPageableData(api + 'tagList', params, sort);
      }}
      size="small"
      columns={this.columns}
      rowSelection={false}
      rowKey="name"
      search={false}
      options={{search: true}}
    />
  }


}



