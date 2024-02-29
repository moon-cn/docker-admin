/**
 * UMI Runtime Config https://umijs.org/docs/runtime-config
 */
import {history} from 'umi';
import {setLogin, setMenu} from "./utils/SysConfig";
import './utils/timesince'
import {get} from "@/utils/request";
import {Modal} from "antd";

export function render(oldRender) {
  let path = history.location.pathname;
  if (path == '/login') {
    oldRender()
    return
  }

  get("api/login/check").then((rs) => {
    setLogin(rs.data)
    oldRender()
  }).catch(() => {
    history.push('/login')
    oldRender()
  })
}


export function onRouteChange({location, routes, action}) {

  Modal.destroyAll()
}

