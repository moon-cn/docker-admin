import React from 'react';
import ContainerLog from "../../components/container/ContainerLog";
import {Tabs} from "antd";
import ContainerFile from "./ContainerFile";
import ContainerCmd from "./ContainerCmd";
import ContainerInfo from "./ContainerInfo";
import ContainerStats from "./ContainerStats";

export default class extends React.Component {


  render() {
    const {containerId, hostId} = this.props;

    return <Tabs tabPosition='left' destroyInactiveTabPane>
      <Tabs.TabPane tab="日志" key="container-log">
        <ContainerLog hostId={hostId} containerId={containerId}/>
      </Tabs.TabPane>


      <Tabs.TabPane tab="文件" key="container-file">
        <ContainerFile hostId={hostId} containerId={containerId}/>
      </Tabs.TabPane>

      <Tabs.TabPane tab="命令" key="container-cmd">
        <ContainerCmd hostId={hostId} containerId={containerId}/>
      </Tabs.TabPane>
      <Tabs.TabPane tab="资源统计" key="container-stats">
        <ContainerStats hostId={hostId} containerId={containerId}/>
      </Tabs.TabPane>
      <Tabs.TabPane tab="详情" key="container-info">
        <ContainerInfo hostId={hostId} containerId={containerId}/>
      </Tabs.TabPane>
    </Tabs>
  }


}



