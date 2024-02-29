import {PlusOutlined} from '@ant-design/icons';
import {Button, Divider, message, Modal, Popconfirm} from 'antd';
import React from 'react';

import {get, getPageableData, post} from "../../utils/request";
import common from "../../utils/common";
import {ProTable} from "@ant-design/pro-components";
import {history} from "umi";
import {notPermitted} from "../../utils/SysConfig";

const addTitle = "添加项目"
const editTitle = '编辑项目'
let api = '/api/gitCredential/';


export default class extends React.Component {

  state = {
    showAddForm: false,
    showEditForm: false,
    formValues: {},

  }
  actionRef = React.createRef();
  columns = [
    {
      title: 'URL',
      dataIndex: 'url',
    },

    {
      title: '帐号',
      dataIndex: 'username',
    },
    {
      title: '密码',
      dataIndex: 'password',
      valueType: 'password',
    },

    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, row) => {
        return <>
          <Button
            type='link'
            disabled={notPermitted('gitCredential:save')}
            onClick={() => {
            this.state.showEditForm = true;
            this.state.formValues = row;
            this.setState({
              showEditForm: true,
              formValues: row
            })
          }}>修改</Button>
          <Divider type={"vertical"} />
          <Popconfirm disabled={notPermitted('gitCredential:delete')} title="确定删除，删除后将不可恢复" onConfirm={()=>this.handleDelete(row)}>
            <a disabled={notPermitted('gitCredential:delete')}>删除</a>
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
  };

  handleUpdate = value => {
    let params = value;
    params.id = this.state.formValues.id

    post(api + 'update', params).then(rs => {
      this.state.showEditForm = false;
      this.setState(this.state)
      this.actionRef.current.reload();
    })
  }
  handleDelete = (row) => {
    get(api + 'delete', {id:row.id}).then(rs => {
      message.info(rs.msg )
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
                <Button             disabled={notPermitted('gitCredential:save')}
                                    type="primary" onClick={() => {
                  this.state.showAddForm = true;
                  this.setState(this.state)
                }}>
                  <PlusOutlined/> 新建
                </Button>,
              ]}
              request={(params, sort) => getPageableData(api + "list" , params, sort)}
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



