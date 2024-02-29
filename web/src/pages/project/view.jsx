import {Card, Descriptions, Spin} from 'antd';
import React from 'react';
import {get, post} from "../../utils/request";
import BuildLogList from "./BuildLogList";

let api = '/api/project/';

export default class extends React.Component {


  state = {
    project: null,
    showTrigger: false,
    triggerValueList: [],
    activeTab: 'jnl'
  }

  componentDidMount() {
    this.id = this.props.location.query.id
    get(api+'get', {id: this.id}).then(rs=>this.setState({project: rs}))
  }

  save = value => {
    value.id = this.state.project.id
    post(api + 'update', value)
  }

  render() {
    if(this.state.project == null){
      return <Spin />
    }

    const {project} = this.state;
    return (<>

      <Card className='mb-2'>
        <Descriptions title={project.name} >
          <Descriptions.Item label='id'>{project.id}</Descriptions.Item>
          <Descriptions.Item label='代码源'>{project.gitUrl}</Descriptions.Item>
          <Descriptions.Item label='dockerfile'>{project.dockerfile}</Descriptions.Item>
          <Descriptions.Item label='默认版本'>{project.defaultVersion}</Descriptions.Item>
          <Descriptions.Item label='分支'>{project.branch}</Descriptions.Item>
          <Descriptions.Item label='创建时间'>{project.createTime}</Descriptions.Item>
        </Descriptions>
      </Card>


     <BuildLogList  project={project}/>


    </>)
  }


}



