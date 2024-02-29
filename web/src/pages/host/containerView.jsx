import React from 'react';
import ContainerBox from "../../components/container/ContainerBox";
import {Card} from "antd";



export default class extends React.Component {

  render() {
    const {containerId, hostId} = this.props.location.query;

    return <Card style={{minHeight:'calc(100vh - 100px)'}}>
      <ContainerBox containerId={containerId} hostId={hostId} />
    </Card>
  }


}



