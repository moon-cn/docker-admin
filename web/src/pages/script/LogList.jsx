import {Divider, Tooltip} from 'antd';
import React from 'react';
import {get, getPageableData} from "../../utils/request";
import {
  CheckCircleFilled,
  ClockCircleOutlined,
  CloseCircleFilled,
  Loading3QuartersOutlined,
  MinusCircleTwoTone
} from "@ant-design/icons";
import {formatTotalTime} from "../../utils/utils";
import {ProTable} from "@ant-design/pro-components";
import {modal} from "../../components/ModalTool";
import StreamLog from "../../components/StreamLog";

let api = '/api/scriptLog/';

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
    this.listURL = api + "list?scriptId=" + props.id
  }

  listURL = null
  state = {
    showLog: false,
    curRow: {}
  }
  actionRef = React.createRef();

  reload = () => {
    this.actionRef.current.reload()
  }

  columns = [
    {
      title: '开始时间',
      dataIndex: 'createTime',
      render(_, row) {
        return <Tooltip title={row.createTime}> {new Date(row.createTime).timeSince()}</Tooltip>
      }
    },
    {
      title: '分支或标签',
      dataIndex: 'value',
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
      render: (_, row) => {
        return <div>

          <a onClick={() => modal({title:'脚本日志',content:<StreamLog url={row.logUrl}/>})}>日志</a>
          <Divider type='vertical'/>

          <a onClick={() => this.stop(row)}>停止</a>
        </div>
      }
    },
  ]


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

  stop = row => {
    get("/api/scriptLog/stop", {id: row.id}).then(rs => {
      this.reload()
    })
  }

  render() {

    return <ProTable
      search={false}
      actionRef={this.actionRef}
      request={(params, sort) => getPageableData(this.listURL, params, sort)}
      columns={this.columns}
      rowSelection={false}
      toolBarRender={false}

      rowKey="id"
    />
  }


}



