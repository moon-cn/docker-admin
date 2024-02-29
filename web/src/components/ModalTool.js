import {Modal} from "antd";
import React from "react";


export function modal(props){
  props = props || {}
  let modal = Modal.info({
    icon: null,
    footer: null,
    closable: true,
    closeIcon: 'x',
    width: '80vw',
    ...props,
  });
  return modal;
}


export function showModal(reactNode, props) {
  props = props || {}
  let modal = Modal.info({
    icon: null,
    footer: null,
    closable: true,
    closeIcon: 'x',
    width: '70vw',
    content: reactNode,
    ...props,

  });
  return modal
}

