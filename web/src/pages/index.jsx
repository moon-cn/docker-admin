import {Button, Card, Col, Row, Statistic, Table} from "antd";
import React from "react";
import {get} from "../utils/request";

export default class extends React.Component {

  state = {
    info:{}
  }

  componentDidMount() {
    get('api/homeInfo').then(rs=>{
      this.setState({info:rs.data})
    })
  }


  render() {
    const {appCount, projectCount, hostCount} = this.state.info
    return <Card style={{margin: 16}}>
      <Row gutter={[24,32]} >
        <Col span={12}>
          <Statistic title="项目" value={projectCount} />
        </Col>

        <Col span={12}>
          <Statistic title="应用" value={appCount}  />
        </Col>
        <Col span={12}>
          <Statistic title="主机" value={hostCount}  />
        </Col>
      </Row>
    </Card>
  }
}
