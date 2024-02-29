import {Modal} from 'antd';
import React from 'react';

import {getPageableData} from "../../utils/request";
import {LazyLog, ScrollFollow} from "react-lazylog";
import {
  CheckCircleFilled,
  ClockCircleOutlined, CloseCircleFilled,
  Loading3QuartersOutlined,
  MinusCircleTwoTone
} from "@ant-design/icons";
import {formatTotalTime} from "../../utils/utils";
import {ProTable} from "@ant-design/pro-components";

let api = '/api/deployLog/';


function getIcon(key, index) {
  const iconDict = {
    PENDING: <ClockCircleOutlined key={index}/>,
    PROCESSING: <Loading3QuartersOutlined key={index} spin/>,
    SUCCESS: <CheckCircleFilled key={index} style={{color: 'green'}}/>,
    ERROR: <CloseCircleFilled key={index} style={{color: 'red'}}/>,
    CANCEL: <MinusCircleTwoTone />
  }
  return iconDict[key]
}


export default class extends React.Component {

  constructor(props) {
    super(props);
    this.listURL = api + "list"
    if(props.project){
        this.listURL += "?projectId=" + props.project.id
    }
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
      render(_,row){
        return new Date(row.createTime).timeSince();
      }
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
      title: '状态',
      dataIndex: 'success',
      render(_, row) {
        let key = 'PROCESSING';

        if(row.success == true){
          key = "SUCCESS";
        }else if(row.success == false){
          key = "ERROR"
        }
        return getIcon(key, 1) ;
      }
    },


    {
      title: '耗时',
      dataIndex: 'timeSpend',
      render(t, row) {
       return  formatTotalTime(t)
      }
    },
    {
      title: '日志',
      dataIndex: 'option',
      valueType: 'option',
      render:(_,row)=>{
        return <a onClick={()=>this.showLog(row)}>日志</a>
      }
    },
  ]
  showLog = (row) => {
    this.setState({showLog: true, curRow: row})
  }

  timer=  null

  componentDidMount() {
      if(this.timer){
        clearInterval(this.timer)
      }


      this.timer = setInterval(()=>{
        this.reload()
      }, 1000 * 30)


  }

  componentWillUnmount() {
    if(this.timer){
      clearInterval(this.timer)
    }
  }


  render() {

    const {curRow} = this.state
    const pageSize = this.props.pageSize || 20

    return (<div>

      <ProTable
        search={false}
        actionRef={this.actionRef}
        request={(params, sort) => getPageableData(this.listURL, params, sort)}
        columns={this.columns}
        rowSelection={false}
        toolBarRender={false}
        pagination={{pageSize:pageSize}}

        rowKey="id"
      />


      <Modal
        width={1200}
        maskClosable={false}
        destroyOnClose
        title="构建的日志"
        visible={this.state.showLog}
        onCancel={() => {
          this.setState({showLog: false})
        }}
        footer={null}
      >

        <div style={{minHeight: 500}}>


          {curRow.logUrl &&    <ScrollFollow
            startFollowing={true}
            render={({follow, onScroll}) => (
              <LazyLog url={curRow.logUrl}
                       fetchOptions={{credentials:'include'}}
                       websocket
                       stream follow={follow} onScroll={onScroll}/>
            )}
          />}

        </div>
      </Modal>


    </div>)
  }


}



