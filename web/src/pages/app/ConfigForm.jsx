import {Button, Col, Divider, Input, message, Row, Select, Spin} from "antd";
import React from "react";
import EditTable from "../../components/EditTable";
import {post} from "../../utils/request";
import CodeMirrorEditor from "../../components/CodeMirrorEditor";


export default class extends React.Component {

  state = {
    form: {
      ports: [],
      binds: [],
      environmentYAML: ''
    },
  }

  constructor(props) {
    super(props);
    const app = this.props.app
    this.state.form = app.config
  }


  portsColumns = [
    {title: '主机端口', dataIndex: 'publicPort', dataType: 'InputNumber'},
    {title: '容器端口', dataIndex: 'privatePort', dataType: 'InputNumber'},
    {title: '协议', dataIndex: 'protocol', dataType: 'Select', valueEnum: {TCP: 'TCP', UDP: 'UDP'}},
  ]

  bindsColumns = [
    {title: '主机路径', dataIndex: 'publicVolume', dataType: 'Input'},
    {title: '容器路径', dataIndex: 'privateVolume', dataType: 'Input'},
  ]
  update = () => {
    const {form} = this.state;
    const hide = message.loading("修改配置中...", 0)
    this.setState({form: null})
    post('/api/app/updateConfig?id=' + this.props.app.id, form).then(rs => {
      const app = rs.data;
      this.setState({form: app.config})
      hide()
      this.props.onChange(app)
    })
  }

  render() {
    const {form} = this.state;
    if (!form) {
      return <Spin/>
    }
    return <>


      <Row>
        <Col flex="100px">
          <h4>网络模式</h4>
        </Col>
        <Col flex="auto">
          <Select
            options={[
              {label: '主机模式（同主机IP）', value: 'host',},
              {label: '桥接（虚拟IP，NAT）', value: 'bridge',},
              {label: '无需网络', value: 'none',},
            ]}
            style={{width: 300}}
            value={form.networkMode}
            onChange={e => {
              form.networkMode = e
              this.setState({form})
            }}
          >

          </Select>
        </Col>
      </Row>
      <Divider/>

      {form.networkMode === 'bridge' && <> <Row>
        <Col flex="100px">
          <h4>端口映射</h4>
        </Col>
        <Col flex="auto">
          <EditTable columns={this.portsColumns} dataSource={form.ports}></EditTable>
        </Col>
      </Row>
        <Divider/>
      </>
      }


      <Row>
        <Col flex="100px">
          <h4>文件映射</h4>
        </Col>
        <Col flex="auto">
          <EditTable columns={this.bindsColumns} dataSource={form.binds}></EditTable>
        </Col>
      </Row>
      <Divider/>

      <Row>
        <Col flex="100px">
          <h4>环境变量</h4>
          <i style={{fontSize: "small"}}>YML格式</i>
        </Col>
        <Col flex="auto">
          <CodeMirrorEditor value={form.environmentYAML}
                            onChange={e => {
                              form.environmentYAML = e
                              this.setState({form})
                            }}/>

        </Col>
      </Row>
      <Divider/>
      <Row>
        <Col flex="100px">
          <h4>启动命令</h4>
        </Col>
        <Col flex="auto">
          <Input value={form.cmd} onChange={e => {
            form.cmd = e.target.value
            this.setState({form})
          }}/>
        </Col>
      </Row>

      <Divider/>

      <Row>
        <Col flex="100px">
          <h4>ExtraHosts</h4>
          <small><i>域名IP映射</i></small>
          <small><i></i></small>
        </Col>
        <Col flex="auto">
          <Input placeholder='域名:IP 域名2:IP2' value={form.extraHosts} onChange={e => {
            form.extraHosts = e.target.value
            this.setState({form})
          }}/>
        </Col>
      </Row>


      <Divider/>
      <Row>
        <Col flex="100px">
        </Col>
        <Col flex="auto">
          <Button type="primary" onClick={this.update} size={"large"}>保存并重启</Button>
        </Col>
      </Row>


    </>

  }
}
