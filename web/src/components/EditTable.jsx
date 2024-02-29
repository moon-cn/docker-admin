import {Input, InputNumber, Select} from "antd";
import React from "react";
import {DeleteOutlined, PlusCircleFilled} from '@ant-design/icons';

export default class extends React.Component {

  state = {
    dataSource: []
  }

  constructor(props) {
    super(props);

    this.state.dataSource = props.dataSource;

  }


  columns = []


  add = () => {
    let {dataSource} = this.state;
    dataSource.push({})

    this.setState({dataSource})
  }
  remove = (i) => {
    let {dataSource} = this.state;

    dataSource.splice(i, 1)

    this.setState({dataSource})
  }

  edit = (key, value, i) => {
    let {dataSource} = this.state;
    dataSource[i][key] = value;
    this.setState({dataSource})
  }


  render() {
    const {columns} = this.props;
    const {dataSource} = this.state
    return <div>
      <table>
        <thead>
        <tr>
          {columns.map(c => <th key={c.dataIndex}>{c.title}</th>)}
          <th></th>
        </tr>
        </thead>
        <tbody>
        {dataSource.map((p, i) => <tr key={i}>
          {columns.map(c => <td key={c.dataIndex}>
            {c.dataType == 'Input' && <Input
                                             value={p[c.dataIndex]}
                                             onChange={e => {
                                               this.edit(c.dataIndex, e.target.value, i)
                                             }}/>}

            {c.dataType == 'InputNumber' && <InputNumber
                                                         value={p[c.dataIndex]}
                                                         onChange={v => {
                                                           this.edit(c.dataIndex, v, i)
                                                         }}/>}


            {c.dataType == 'Select' && <Select value={p[c.dataIndex]}
                                               onChange={v => this.edit(c.dataIndex, v, i)}
                                               style={{minWidth: 100}}
            >

              {Object.keys(c.valueEnum).map(k => <Select.Option key={k} value={k}>{c.valueEnum[k]}</Select.Option>)}
            </Select>}
          </td>)}
          <td>
            <DeleteOutlined onClick={() => this.remove(i)}></DeleteOutlined>
          </td>
        </tr>)}

        {dataSource.length == 0 && <tr>
          <td height={50} colSpan={columns.length + 1}>暂无数据</td>
        </tr>}

        </tbody>
      </table>

      <div style={{marginTop: 16, marginBottom: 16}}>
          <PlusCircleFilled style={{color: '#1890ff'}}/><a onClick={this.add}>添加</a>
      </div>
    </div>

  }
}
