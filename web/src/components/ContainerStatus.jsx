import {Tag} from "antd";
import React from "react";
import {get} from '../utils/request'

/**
 * 容器状态
 */
export default class extends React.Component {

  state = {
    status: '-'
  }

  componentDidMount() {
    const {hostId, appName} = this.props
    get("api/container/status", {hostId, appName}, true).then(rs => {
      this.setState({status: rs.msg})
    })
  }

  render() {
    const s = this.state.status;
    if (s && s.indexOf('Up') >= 0) {
      return <Tag color={"green"}>{s} </Tag>
    }
    return <Tag color={"red"}>{s}</Tag>
  }
}
