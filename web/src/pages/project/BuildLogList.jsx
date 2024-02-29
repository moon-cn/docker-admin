import {Button, Divider, Form, Input, Modal, Space, Switch, Tooltip} from 'antd';
import React from 'react';
import {
  CheckCircleFilled,
  ClockCircleOutlined,
  CloseCircleFilled,
  Loading3QuartersOutlined,
  MinusCircleTwoTone
} from "@ant-design/icons";
import {formatTotalTime} from "../../utils/utils";
import {ProTable} from "@ant-design/pro-components";
import {get, getPageableData} from "../../utils/request";
import moment from "moment";
import StreamLog from "../../components/StreamLog";

let api = '/api/buildLog/';

function getIcon(key, index) {
  const iconDict = {
    PENDING: <ClockCircleOutlined key={index}/>,
    PROCESSING: <Loading3QuartersOutlined key={index} spin/>,
    SUCCESS: <CheckCircleFilled key={index} style={{color: 'green'}}/>,
    ERROR: <CloseCircleFilled key={index} style={{color: 'red'}}/>,
    CANCEL: <MinusCircleTwoTone/>
  }
  return iconDict[key]
}


export default class extends React.Component {

  constructor(props) {
    super(props);
    this.listURL = api + "list"
    if (props.project) {
      this.listURL += "?projectId=" + props.project.id
      this.projectId = props.project.id
    }
  }


  listURL = null
  state = {
    curRow: {},

    showTrigger: false,
  }
  actionRef = React.createRef();

  reload = () => {
    this.actionRef.current.reload()
  }

  columns = [
    {
      title: '项目',
      dataIndex: 'projectName',
    },
    {
      title: '开始时间',
      dataIndex: 'createTime',
      render(_, row) {
        return <Tooltip title={row.createTime}> {new Date(row.createTime).timeSince()}</Tooltip>
      }
    },
    {
      title: '分支/标签',
      dataIndex: 'value',
    },
    {
      title: '目录',
      dataIndex: 'context',
    },
    {
      title: 'Dockerfile',
      dataIndex: 'dockerfile',
    },
    {
      title: '版本',
      dataIndex: 'version',
    },
    {
      title: '代码日志',
      dataIndex: 'codeMessage',
    },
    {
      title: '构建主机',
      dataIndex: 'buildHostName',
    },
    {
      title: '状态',
      dataIndex: 'success',
      render(_, row) {
        let key = 'PROCESSING';

        if (row.success == true) {
          key = "SUCCESS";
        } else if (row.success == false) {
          key = "ERROR"
        }
        return getIcon(key, 1);
      }
    },


    {
      title: '耗时',
      dataIndex: 'timeSpend',
      render(t, row) {
        return formatTotalTime(t)
      }
    },
    {
      title: '-',
      dataIndex: 'option',
      valueType: 'option',
      fixed:'right',
      render: (_, row) => {
        return <div>
          <a onClick={() => {
            Modal.info({
              title:'构建日志',
              width:1024,
              closable:true,
              okText:'关闭',
              icon:null,
              content:<StreamLog url={row.logUrl}/>
            })
            }}>日志</a>

          <Divider type='vertical'/>

          <a onClick={() => this.stop(row)}>停止</a>
          <Divider type='vertical'/>
          <a onClick={() => this.retry(row)}>重试</a>
        </div>
      }
    },
  ]

  retry = row => {
    get("/api/project/build", row).then(rs => {
      this.reload()
    })
  }

  stop = row => {
    get("/api/project/stopBuild", row).then(rs => {
      this.reload()
    })
  }

  timer = null

  componentDidMount() {
    if (this.timer) {
      clearInterval(this.timer)
    }


    this.timer = setInterval(() => {
      this.reload()
    }, 1000 * 30)


  }

  componentWillUnmount() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
  triggerPipeline = () => {
    this.setState({showTrigger: true})
  }


  submitTrigger = (values) => {
    get("/api/project/build", values).then(rs => {
      this.setState({showTrigger: false})
      this.actionRef.current.reload()
    })
  }
  cleanError = () => {
    get("/api/project/cleanErrorLog", {id:this.projectId}).then(rs => {
      this.actionRef.current.reload()
    })
  }

  render() {
    const {project } = this.props;
    const {showTrigger} = this.state

    return (<>

      <ProTable
        headerTitle='构建记录'
        toolBarRender={()=> {
          return <Space><Button onClick={this.triggerPipeline} type="primary">立即构建</Button>
            <Button onClick={this.cleanError} >清理</Button>
          </Space>;
        }}
        search={false}
        actionRef={this.actionRef}
        request={(params, sort) =>getPageableData(this.listURL, params, sort)}
        columns={this.columns}
        rowSelection={false}
        scroll={{x:'max-content'}}

        rowKey="id"
      />



      <Modal open={showTrigger} title="手动触发流水线"
             destroyOnClose={true}
             footer={null}
             onCancel={() => this.setState({showTrigger: false})}>

        <Form
              onFinish={this.submitTrigger}
              labelCol={{flex:'100px'}}
              initialValues={{
                value: project.branch || 'master',
                version: project.defaultVersion || 'v' + moment().format('YYYYMMDD'),
                dockerfile: project.dockerfile || 'Dockerfile',
                projectId: project.id
              }}
              preserve={false}>
          <Form.Item name="projectId" hidden>
          </Form.Item>
          <Form.Item name="value" label="分支、标签">
            <Input/>
          </Form.Item>
          <Form.Item name="version" label="版本">
            <Input/>
          </Form.Item>
          <Form.Item name="dockerfile" label="dockerfile">
            <Input/>
          </Form.Item>
          <Form.Item name="useCache" label="使用缓存" >
            <Switch defaultChecked />
          </Form.Item>

          <div style={{display:'flex',justifyContent:'end'}}>
            <Button type='primary' htmlType="submit">确定</Button>

          </div>

        </Form>
      </Modal>

    </>)
  }


}



