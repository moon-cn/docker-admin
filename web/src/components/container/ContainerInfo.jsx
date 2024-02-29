import React from "react";
import {get} from "../../utils/request";
import {Descriptions, Spin} from "antd";
import {Empty} from "antd/lib";
import CodeMirrorEditor from "../CodeMirrorEditor";



export default class extends React.Component {

  state = {
    data: null
  }

  componentDidMount() {
    let {hostId, containerId} = this.props;
    get('/api/container/get', {hostId, containerId}).then(rs => {
      this.setState({data: rs.data})
    })
  }

  render() {
    let data = this.state.data;

    if (!data) {
      return <Spin/>
    }

    return  <pre className='bg-black text-green-500'>{this.state.data}</pre>
  }


}
