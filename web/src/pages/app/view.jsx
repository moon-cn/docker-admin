import {
  Alert,
  AutoComplete,
  Button,
  Card,
  Col,
  Descriptions,
  Divider,
  Input,
  message,
  Modal,
  Row,
  Space,
  Spin,
  Switch,
  Tabs,
  Tag,
  Typography
} from 'antd';
import React from 'react';
import ConfigForm from "./ConfigForm";
import RemoteSelect from "../../components/RemoteSelect";
import {get, post} from "../../utils/request";
import {history} from "umi";
import ContainerBox from "../../components/container/ContainerBox";
import {showResult} from "../../utils/utils";
import {notPermitted} from "../../utils/SysConfig";
import StreamLog from "../../components/StreamLog";

let api = '/api/app/';


export default class extends React.Component {

  state = {
    loading: true,
    app: {},
    container: {},
    containerNotFound: false,

    tagOptions: [],

    tabKey: undefined,

    moveApp: {
      targetHostId: null
    },
    publishApp: {
      targetVersion: null
    },
    showEditName: false,
    newName: ''


  }


  componentDidMount() {
    let id = this.props.location.query.id;
    this.id = id;

    get('api/app/get', {id: id}).then(rs => {
      this.setState({app: rs, loading: false});
      this.loadTagOptions(rs.imageUrl)
    })


    this.timerId =setInterval(this.loadContainer, 5000)
    this.loadContainer();
  }

  componentWillUnmount() {
    clearInterval(this.timerId)
  }


  loadContainer = () => {
    get("/api/app/container", {id: this.id},true).then(rs => {
      const container = rs.data;
      this.setState({container, containerNotFound:false})

    }).catch(() => {
      this.setState({containerNotFound: true})
    })
  }
  loadTagOptions(url) {
    if (url) {
      get('api/repository/tagOptions', {url}).then(rs => {
        this.setState({tagOptions: rs})
      })
    }
  }
  deploy = () => {
    post('api/app/deploy/' + this.state.app.id).then(showResult).catch(showResult)
  }
  start = () => {
    post('api/app/start/' + this.state.app.id)
  }
  stop = () => {
    post('api/app/stop/' + this.state.app.id)
  }

  setAutoDeploy = (id, autoDeploy) => {
    get("/api/app/autoDeploy", {id, autoDeploy})
  }
  setAutoRestart = (id, autoRestart) => {
    get("/api/app/autoRestart", {id, autoRestart})
  }
  moveApp = () => {
    const id = this.state.app.id;
    const hostId = this.state.moveApp.targetHostId;
    get("/api/app/moveApp", {id, hostId}).then(rs => {
      window.location.reload(true)
    })
  }

  updateVersion = () => {
    const id = this.state.app.id;
    const tag = this.state.publishApp.targetVersion;
    get("/api/app/updateVersion", {id, version: tag}).then(rs => {
      message.success(rs.msg)
      window.location.reload(true)
    })
  }


  handleDelete = () => {
    const id = this.state.app.id
    const hide = message.loading('删除中...')
    get(api + 'delete', {id}).then(rs => {
      message.info(rs.msg)
      hide();

      history.push('/app')

    }).catch(rs => {
      hide();
      Modal.confirm({
        title: '删除失败',
        content: '是否强制删除数据',
        okText: '强制删除数据',
        cancelText: '取消',
        onOk: () => {
          get(api + 'delete', {id, force: true}).then(rs => {
            message.info(rs.message)
            history.push('/app')
          })
        }
      })

    })
  }

  rename = () => {
    let appId = this.state.app.id;
    let {newName} = this.state;
    const hide = message.loading('指令发送中...')
    this.setState({tabKey:'deploy-log'})
    post(api + 'rename' , {appId, newName}).then(rs => {

      message.success(rs.msg)
      this.setState({app: rs.data, showEditName: false})
    }).finally(hide)
  }

  render() {
    const {container, app, loading} = this.state;

    if (loading) {
      return <Spin/>
    }
    const {state} = container;


    return (<>

      <Card title={app.name} extra={<Space>
        {state == 'exited' && <Button onClick={this.start} type="primary">启动</Button>}
        {state == 'running' && <Button onClick={this.stop} type="primary" danger>停止</Button>}
        <Button onClick={this.deploy} type="primary">重新部署</Button>
      </Space>}>


        <Descriptions size="small">
          <Descriptions.Item label='应用'>  {app.name} </Descriptions.Item>
          <Descriptions.Item label='主机'>  {app.host.name} </Descriptions.Item>
          <Descriptions.Item label='镜像' span={2}>  {app.imageUrl}:{app.imageTag} </Descriptions.Item>
          <Descriptions.Item label='容器'>  {container.name}         </Descriptions.Item>

          <Descriptions.Item label='状态'>
            <Tag color={container.state == 'running' ? 'green' : 'red'}>
              {container.status}</Tag>
          </Descriptions.Item>

          <Descriptions.Item label='创建于'>  { new Date( app.createTime).timeSince()} </Descriptions.Item>

        </Descriptions>

      </Card>


      <Card className='mt-2'>
        {this.renderTabs()}
      </Card>


    </>)
  }

  renderTabs = () => {
    const {container, containerNotFound} = this.state;
    const {app} = this.state



    return <>
      {containerNotFound && <Alert type={"error"} message="容器部署中..."/>}
      <Tabs  activeKey={this.state.tabKey}  onChange={key=>this.setState({tabKey:key})} destroyInactiveTabPane defaultActiveKey='deploy-log'>
        <Tabs.TabPane tab="容器" key="container">
          {container.id &&    <ContainerBox containerId={container.id} hostId={app.host.id} />}
        </Tabs.TabPane>


        <Tabs.TabPane tab="部署日志" key="deploy-log">
          <StreamLog url={"/api/log/" + app.id} />
        </Tabs.TabPane>

        <Tabs.TabPane tab="配置" disabled={notPermitted('app:config')} key="2">
          <ConfigForm app={app} onChange={app => {
            window.location.reload(true)
          }}/>
        </Tabs.TabPane>


        <Tabs.TabPane tab="发布" key="publish">


          <Row wrap={false}>
            <Col flex="100px">自动发布</Col>
            <Col flex="auto">
              <Switch checked={app.autoDeploy}
                      onChange={checked => {
                        app.autoDeploy = checked
                        this.setState({app: this.state.app})
                        this.setAutoDeploy(app.id, checked)
                      }}
              ></Switch>
              <div>
                <Typography.Text type="secondary">当有镜像构建成功后，自动更新应用到最新构建的版本</Typography.Text>
              </div>
            </Col>
          </Row>
          <Divider></Divider>

          <Row wrap={false}>
            <Col flex="100px">手动发布</Col>
            <Col flex="auto">
              <AutoComplete  options={this.state.tagOptions}
                             style={{width: 150}}
                             value={this.state.publishApp.targetVersion}
                             onChange={targetVersion => {
                this.setState({publishApp: {targetVersion}})
              }}/>

              &nbsp;&nbsp;
              <Button type={"primary"} onClick={this.updateVersion}>更新应用</Button>

              <div>
                <Typography.Text type="secondary">用指定的镜像版本</Typography.Text>
              </div>
            </Col>
          </Row>

        </Tabs.TabPane>

        <Tabs.TabPane tab="迁移" key="move" disabled={notPermitted('app:moveApp')}>
          <Row wrap={false}>
            <Col flex="100px">迁移应用</Col>
            <Col flex="auto">
                <Alert message="应用会迁移到下列任意一台主机中" type="warning"></Alert>

              <br />

                <RemoteSelect url="/api/host/options"
                              style={{width: 300}}
                              placeholder="请选择"
                              showSearch
                              value={this.state.moveApp.targetHostId} onChange={targetHostId => {
                  this.setState({moveApp: {targetHostId}})
                }} />

              &nbsp;&nbsp;

                <Button type={"primary"} onClick={this.moveApp}>迁移</Button>
            </Col>
          </Row>
        </Tabs.TabPane>


        <Tabs.TabPane tab="设置" key="setting" disabled={notPermitted('app:config')}>
          <Row wrap={false}>
            <Col flex="100px">名称</Col>
            <Col flex="auto">

              {!this.state.showEditName ? <div>
                {this.state.app.name} <a onClick={() => this.setState({
                newName: this.state.app.name,
                showEditName: true
              })}>修改名称</a>
              </div> : <div>

                <Input value={this.state.newName} style={{width: 200}}
                       onChange={e => this.setState({newName: e.target.value})}></Input>

                <Button type={"primary"} onClick={this.rename}>确定</Button>
              </div>}

            </Col>

          </Row>


          <Divider></Divider>
          <Row wrap={false}>
            <Col flex="100px">自动重启</Col>
            <Col flex="auto">
              <Switch checked={app.autoRestart}
                      onChange={checked => {
                        app.autoRestart = checked
                        this.setState({app: this.state.app})
                        this.setAutoRestart(app.id, checked)
                      }}
              ></Switch>
            </Col>

          </Row>


          <Divider></Divider>
          <Row wrap={false}>
            <Col flex="100px">删除应用</Col>
            <Col flex="auto">
              <Space direction={"vertical"}>
                <Alert message="请注意，删除应用将清除该应用的所有数据，且该操作不能被恢复，您确定要删除吗?"
                       type="warning"
                ></Alert>
                <Button danger type="primary" onClick={this.handleDelete}>删除应用</Button>
              </Space>
            </Col>
          </Row>


        </Tabs.TabPane>
      </Tabs>
    </>
  }

}



