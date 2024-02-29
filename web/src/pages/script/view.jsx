import {Button, Card, Descriptions, Form, Input, Modal, Spin} from 'antd';
import React from 'react';
import {get, post} from "../../utils/request";
import LogList from "./LogList";

let api = '/api/script/';

export default class extends React.Component {

  state = {
    loading: true,
    script: {},
    project: {},
    showTrigger: false,
    triggerValueList: [],
    activeTab: 'jnl'
  }
  triggerFormRef = React.createRef();

  componentDidMount() {
    let id = this.props.location.query.id;

    get(api + "get", {id}).then(script => {
      this.setState({script, project: script.project, loading: false})
    })
  }

  triggerRun = () => {
    this.setState({showTrigger: true})
  }

  logRef = React.createRef();

  submitTrigger = () => {
    this.triggerFormRef.current.validateFields().then(values => {

      get("/api/script/run", values).then(rs => {
        this.setState({showTrigger: false, activeTab: 'jnl'})
        if (this.logRef.current) {
          this.logRef.current.reload();
        }

      })
    })
  }



  render() {
    const {loading, project, script, showTrigger} = this.state;
    if (loading) {
      return <Spin/>
    }
    return (<>

      <Card title={script.name} extra={<Button onClick={this.triggerRun} type="primary">立即运行</Button>}>
        <Descriptions>
          <Descriptions.Item label='关联项目'>{project.name}</Descriptions.Item>
          <Descriptions.Item label='代码源'>{project.gitUrl}</Descriptions.Item>
          <Descriptions.Item label='执行主机'>{script.host?.name}</Descriptions.Item>

          <Descriptions.Item label='更新时间'>{script.modifyTime}</Descriptions.Item>

        </Descriptions>

      </Card>

      <Card className='mt-2'>
        <LogList ref={this.logRef} id={script.id}/>
      </Card>



      <Modal open={showTrigger} title="立即允许脚本"
             onOk={this.submitTrigger}
             destroyOnClose={true}
             onCancel={() => this.setState({showTrigger: false})}>
        <Form ref={this.triggerFormRef}
              layout="horizontal"
              labelCol={{span: 6}}
              initialValues={{
                id: script.id,
                value: project.branch || 'master',
              }}
              preserve={false}>
          <Form.Item name="id" noStyle> </Form.Item>
          <Form.Item name="value" label="分支、标签">
            <Input/>
          </Form.Item>

        </Form>
      </Modal>

    </>)
  }


}



