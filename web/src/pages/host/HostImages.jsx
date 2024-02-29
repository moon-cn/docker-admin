import {Button, message, Table} from 'antd';
import React from 'react';
import {get} from "../../utils/request";

let api = '/api/host/';


export default class extends React.Component {

  state = {
    list: [],
    loadingMap: {}
  }

  componentDidMount() {

    this.loadData();
  }

  loadData() {
    let params = this.props;
    const hide = message.loading("加载主机镜像中...", 0)
    get(api + "images", params).then(list => {
      this.setState({list})
      hide()
    }).catch(hide)
  }

  delete = imageId => {
    let map = this.state.loadingMap;
    map[imageId] = true;
    this.setState({loadingMap: map})
    let params = this.props;
    const hide = message.loading("删除镜像中...", 0)
    get(api + "/deleteImage", {id: params.id, imageId}).then(rs => {
      let map = this.state.loadingMap;
      map[imageId] = false;
      this.setState({loadingMap: map})

      message.success(rs.msg)
      this.loadData()
      this.setState({})
      hide()
    }).catch(() => {
      let map = this.state.loadingMap;
      map[imageId] = false;
      this.setState({loadingMap: map})
      hide();
    })
  }

  columns = [
    {
      title: '序号',
      dataIndex: 'index',
      render(tags, row, index) {
        return index + 1;
      }
    },
    {
      title: 'Id',
      dataIndex: 'Id',
      render(v) {
        return v.substr(7, 12);
      }
    },
    {
      title: '镜像Tag',
      dataIndex: 'RepoTags',
      render(tags, row) {
        return tags.map(tag => <div>{tag}</div>)
      }
    },

    {
      title: '创建于',
      dataIndex: 'Created',
      render: function (v, row) {
        let date = new Date(v * 1000);
        return date.toLocaleDateString();
      }
    },
    {
      title: '大小',
      dataIndex: 'Size',
      render(v) {
        return (v / 1024 / 1024).toFixed(1) + " MB";
      }
    },

    {
      dataIndex: 'action',
      fixed:'right',
      width: 200,
      render: (_, row) => {
        const map = this.state.loadingMap;
        const result = map[row.Id]


        return <Button size={"small"} loading={result} onClick={() => this.delete(row.Id)}>删除 {result != null && '已点击'}</Button>;
      }
    },
  ];

  render() {
    return <Table dataSource={this.state.list}
                  rowKey="Id"
                  size={"small"}
                  columns={this.columns}
                  scroll={{x: 'max-content'}}

    />


  }


}



