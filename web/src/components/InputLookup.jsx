import React from 'react';
import {Input} from 'antd';
import {showModal} from "./ModalTool";

export default class extends React.Component {

  showDropdown = () => {
    const {config} = this.props


    const props = {
      onChange: this.handleSelect,
      ...config.props
    }

    const node = React.createElement(config.type, props)


    this.modal = showModal(node, {title:'选择框'})
  };

  handleSelect = (value) => {
    this.props.onChange(value)
    this.modal?.destroy()
  };

  render() {
    const {config, ...rest} = this.props

    return <Input.Search onSearch={this.showDropdown} {...rest} />
  }
}


