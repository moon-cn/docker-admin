import React, {Component} from "react";
import {Input} from "antd";
import {get} from "../../utils/request";
import {KeyOutlined} from "@ant-design/icons";

export default class  extends  React.Component {

  state = {
    data: '请输入Linux命令， 如 ls -l',
    cmdLoading:false
  }
  handleSend = (cmd)=>{
    console.log(cmd)
    let {hostId, containerId} = this.props;

    this.setState({cmdLoading:true})
    get('/api/container/cmd', {hostId, containerId,cmd}).then(rs=>{
      this.setState({data:rs.data})
      this.setState({cmdLoading:false})
    })
  }
  render() {
    return <div>
      <Input.Search placeholder='请输入Linux命令' onSearch={this.handleSend}  loading={this.state.cmdLoading}  />
      <pre className='bg-black text-green-500 p-2' style={{minHeight: 300}}>
          {this.state.data}
      </pre>


    </div>
  }
}
