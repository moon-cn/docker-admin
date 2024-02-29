import {Alert, Button, Modal, Typography} from 'antd';
import React from 'react';

import {ProTable} from "@ant-design/pro-components";
import {getPageableData} from "../../utils/request";
import {showModal} from "../../components/ModalTool";
import AppDeploy from "../app/AppDeploy";

let api = '/api/dockerHub/';


export default class extends React.Component {

  columns = [
    {
      title: '名称',
      dataIndex: 'name',
      render: (name, row) => {
        let url = 'https://hub.docker.com/_/' + name
        return <a href={url} target="_blank">{name} &nbsp;
          </a>
      }
    },

    {
      title: '官方认证',
      dataIndex: 'official',
      render(_,row){
        return row.official && <Typography.Text type={"success"}>官方认证</Typography.Text>
      }
    },

    {
      title: '描述',
      dataIndex: 'description'
    },

    {
      title: '赞',
      dataIndex: 'starCount',
    },
    {
      title: '-',
      render: (_, row) => {
        return <>
          <Button onClick={() => this.setState({row,deployVisible:true}) }>部署应用</Button>
        </>
      }
    },
  ];

  state = {
    row: {},
    deployVisible:false
  }

  render() {

    return <>
      <ProTable
      request={(params, sort) => getPageableData(api + 'list', params, sort)}
      columns={this.columns}
      rowSelection={false}
      search={false}
      options={{search: true}}
      rowKey="name"
      pagination={false}
    />

      <Modal title='部署应用' open={this.state.deployVisible} onCancel={()=>this.setState({deployVisible:false})} destroyOnClose={true} footer={null}>
        <AppDeploy  url={this.state.row.name}  />
      </Modal>

    </>


  }


}



