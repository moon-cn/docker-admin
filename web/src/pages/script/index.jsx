import {PlusOutlined} from '@ant-design/icons';
import {Button, Dropdown, Menu, Modal, Popconfirm} from 'antd';
import React from 'react';

import {get, getPageableData, post} from "../../utils/request";
import common from "../../utils/common";
import RemoteSelect from "../../components/RemoteSelect";
import {ProTable} from "@ant-design/pro-components";
import {history} from "umi";
import CodeMirrorEditor from "../../components/CodeMirrorEditor";

const addTitle = "添加脚本"
const editTitle = '编辑脚本'
let api = '/api/script/';


export default class extends React.Component {

  state = {
    showAddForm: false,
    showEditForm: false,
    formValues: {},

  }
  actionRef = React.createRef();

  columns = [
    {
      title: '脚本名称',
      dataIndex: 'name',

      render: (name, row) => {
        return <a onClick={() => history.push('script/view?id=' + row.id)}>{name}</a>
      }
    },
    {
      title: '关联项目',
      dataIndex: ['project', 'id'],
      sorter: true,
      render: (_, row) => {
        return row?.name
      },
      renderFormItem: () => {
        return <RemoteSelect url={'api/project/options'}></RemoteSelect>
      }
    },


    {
      title: '脚本内容',
      dataIndex: 'content',
      hideInSearch: true,
      hideInTable: true,
      tooltip:'格式为dockerfile',
      renderFormItem: () => {
        return <CodeMirrorEditor mode='dockerfile'/>
      }
    },
    {
      title: '执行主机',
      dataIndex: ['host', 'id'],
      sorter: true,
      render: (_, row) => {
        return row?.name
      },
      renderFormItem: () => {
        return <RemoteSelect url={'api/host/options'}></RemoteSelect>
      }
    },
   /* {
      title: '定时表达式',
      dataIndex: 'cron',
    },*/
    {
      title: '最近更新',
      dataIndex: 'modifyTime',
      sorter: true,
      hideInSearch: true,
      hideInForm: true,
    },


    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, row) => {
        let menu = <Menu>
          <Menu.Item key="1" onClick={() => {
            this.state.showEditForm = true;
            this.state.formValues = row;
            this.setState({
              showEditForm: true,
              formValues: row
            })
          }}>修改</Menu.Item>
          <Popconfirm title="确定删除，删除后将不可恢复" onConfirm={() => this.handleDelete(row)}>
            <Menu.Item key="2">删除</Menu.Item>
          </Popconfirm>

        </Menu>;
        return <Dropdown.Button overlay={menu}
                                onClick={() => history.push('script/view?id=' + row.id)}>查看详情</Dropdown.Button>

      },
    },
  ];
  handleSave = value => {
    post(api + 'save', value).then(rs => {
      this.state.showAddForm = false;
      this.setState(this.state)
      this.actionRef.current.reload();
    })
  }

  handleUpdate = value => {
    let params = {...this.state.formValues, ...value};

    post(api + 'update', params).then(rs => {
      this.state.showEditForm = false;
      this.setState(this.state)
      this.actionRef.current.reload();
    })
  }
  handleDelete = (row) => {
    get(api + 'delete', {id: row.id}, '删除数据').then(rs => {
      this.actionRef.current.reload()
    })
  }


  render() {
    let {showAddForm, showEditForm} = this.state

    return (<>

      <ProTable
        toolBarRender={() => {
          return <Button type="primary" onClick={() => {
            this.state.showAddForm = true;
            this.setState(this.state)
          }}>
            <PlusOutlined/> 新建
          </Button>
        }}
        actionRef={this.actionRef}
        search={false}
        request={(params, sort) => getPageableData(api + "list", params, sort)}
        columns={this.columns}
        rowKey="id"
        bordered={true}
        options={{search: true}}
      />


      <Modal
        maskClosable={false}
        destroyOnClose
        title={addTitle}
        open={showAddForm}
        width={800}
        onCancel={() => {
          this.state.showAddForm = false;
          this.setState(this.state)
        }}
        footer={null}
      >
        <ProTable
          {...common.getTableFormProps()}
          onSubmit={this.handleSave}
          columns={this.columns}
        />
      </Modal>


      <Modal
        maskClosable={false}
        destroyOnClose
        title={editTitle}
        visible={showEditForm}
        width={800}
        onCancel={() => {
          this.state.showEditForm = false;
          this.setState(this.state)
        }}
        footer={null}
      >
        <ProTable
          {...common.getTableFormProps(this.state.formValues)}
          onSubmit={this.handleUpdate}
          columns={this.columns}
        />
      </Modal>
    </>)
  }


}



