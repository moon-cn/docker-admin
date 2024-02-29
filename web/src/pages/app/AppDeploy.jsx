import React from "react";
import {get, post} from "../../utils/request";
import RemoteSelect from "../../components/RemoteSelect";
import {AutoComplete, Button, Form, Input, message} from "antd";
import {history} from "umi";


let api = '/api/app/';

export default class extends React.Component {


  state = {
    tagOptions: []
  }


  formRef = React.createRef()

  handleSave = value => {
    post(api + 'save', value).then(rs => {
      message.success(rs.msg)
      history.push('/app/view?id='+rs.data)
    })
  }

  componentDidMount() {
    this.loadTagOptions()
  }

  loadTagOptions() {
    const url = this.formRef.current?.getFieldValue('imageUrl') || this.props.url;

    if (url) {
      get('api/repository/tagOptions', {url}).then(rs => {
        this.setState({tagOptions: rs})
      })
    }
  }

  render() {
    let defaultUrl = this.props.url;

    return <>
      <Form
        ref={this.formRef}
        initialValues={{
          imageUrl: defaultUrl
        }}
        onValuesChange={changedValues => {
          if (changedValues.imageUrl != null) {
            this.loadTagOptions();
          }
        }}
        onFinish={this.handleSave}
      >
        <Form.Item name='name' label='名称' required rules={[{required: true}]}>
          <Input/>
        </Form.Item>
        <Form.Item name='hostId' label='主机' required rules={[{required: true}]}>
          <RemoteSelect showSearch url="/api/host/options"/>
        </Form.Item>


        <Form.Item name='imageUrl' label='镜像' required rules={[{required: true}]}>
          <RemoteSelect url='/api/repository/options' disabled={defaultUrl}></RemoteSelect>
        </Form.Item>


        <Form.Item name='imageTag' label='版本' required rules={[{required: true}]}>
          <AutoComplete options={this.state.tagOptions} />
        </Form.Item>


        <Form.Item>
          <Button htmlType='submit' type='primary'>确定</Button>
        </Form.Item>
      </Form>

    </>

  }


}
