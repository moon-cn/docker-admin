import {PlusOutlined} from '@ant-design/icons';
import {Button, Divider, Input, message, Modal, Popconfirm, Select} from 'antd';
import React from 'react';

import {get, getPageableData, post} from "../../../utils/request";
import common from "../../../utils/common";
import {ProTable} from "@ant-design/pro-components";

const addTitle = "添加用户"
const editTitle = '编辑用户'
let api = '/api/user/';


export default class extends React.Component {

  state = {
    showAddForm: false,
    showEditForm: false,
    formValues: {},

    roleOptions: [],
    tenantOptions: [],


  }


  componentDidMount() {
    get('api/user/roleOptions').then(rs => {
      let roleOptions = rs.data;

      this.setState({roleOptions})
    })
    get('api/tenant/options').then(rs => {
      let tenantOptions = rs.data;

      this.setState({tenantOptions})
    })

  }

  actionRef = React.createRef();
  columns = [
    {
      title: '姓名',
      dataIndex: 'name',
    },
    {
      title: '账号',
      dataIndex: 'username',
    },
    {
      title: '密码',
      dataIndex: 'password',
      renderFormItem: () => {
        return <Input.Password placeholder='不修改则留空'></Input.Password>
      },
      hideInTable: true,
    },
    {
      title: '角色',
      dataIndex: 'role',
      renderFormItem: () => {
        return <Select options={this.state.roleOptions} allowClear/>
      },
      render: (v) => {
        return this.state.roleOptions.find(r => r.value == v)?.label || v
      }
    },

    {
      title: '租户',
      dataIndex: ['tenant', 'code'],
      renderFormItem: () => {
        return <Select options={this.state.tenantOptions} allowClear/>
      },
      render: (v) => {
        return this.state.tenantOptions.find(r => r.value == v)?.label || v
      }
    },

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
        return <>
          <a onClick={() => {
            this.state.showEditForm = true;
            this.state.formValues = row;
            this.setState({
              showEditForm: true,
              formValues: row
            })
          }}>修改</a>
          <Divider type={"vertical"}/>
          <Popconfirm title="确定删除，删除后将不可恢复" onConfirm={() => this.handleDelete(row)}>
            <a>删除</a>
          </Popconfirm>

        </>
      },
    },
  ];
  handleSave = value => {
    post(api + 'save', value).then(rs => {
      this.state.showAddForm = false;
      this.setState(this.state)
      this.reload();
    })
  }

  reload = () => {
    this.actionRef.current.reload();
  }

  handleUpdate = values => {
    let params = values;
    params.id = this.state.formValues.id

    post(api + 'update', params).then(rs => {
      this.state.showEditForm = false;
      this.setState(this.state)
      this.actionRef.current.reload();
    })
  }
  handleDelete = (row) => {
    get(api + 'delete', {id: row.id}).then(rs => {
      message.info(rs.msg)
      this.actionRef.current.reload();
    })
  }


  render() {
    let {showAddForm, showEditForm} = this.state

    return (<>
      <ProTable
        actionRef={this.actionRef}
        search={false}
        toolBarRender={(action, {selectedRows}) => [
          <Button type="primary" onClick={() => {
            this.state.showAddForm = true;
            this.setState(this.state)
          }}>
            <PlusOutlined/> 新建
          </Button>,
        ]}
        request={(params, sort) => getPageableData(api + "list", params, sort)}
        columns={this.columns}
        rowSelection={false}
        rowKey="id"
        bordered={true}
        options={{search: true}}

      />


      <Modal
        maskClosable={false}
        destroyOnClose
        title={addTitle}
        visible={showAddForm}
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



