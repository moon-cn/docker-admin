import {Divider, message, Select, Spin} from 'antd';

import React from "react";
import {get} from "../../utils/request";

const {Option} = Select;


class RemoteSelect extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      url: props.url,
      data: [],
      value: [],
      fetching: false,
      searchText: null,
    };
  }

  componentDidMount() {
    get(this.props.url).then(data=>{
       this.setState({data:data})
    })
  }




  render() {
    const {url,...rest} = this.props;
    return (
      <Select
        showSearch={true}
        options={this.state.data}

        {...rest}
     />
    );
  }
}

export default RemoteSelect
