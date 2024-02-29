import axios from "axios";
import {message} from "antd";

axios.defaults.withCredentials = true
axios.interceptors.response.use(res => {
  return res.data;
})
axios.interceptors.response.use(rs => {
  if (rs == null) {
    return null;
  }
  const {msg, success} = rs;
  if (success == undefined) {
    return rs
  }

  if (success) {
    return rs;
  } else {
    message.error(msg)
    return Promise.reject(msg)
  }
})

/**
 *
 * @param url
 * @param params
 * @param quiet 是否案件， 不提示操作结果
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export function get(url, params) {
  return axios.get(url, {params})
}

export function post(url, data) {
  return axios.post(url, data)
}

export function getPageableData(url, params, sort) {
  params.pageNumber = params.current;
  delete params.current
  if (sort) {
    let keys = Object.keys(sort);
    if (keys.length > 0) {
      let key = keys[0];
      let dir = sort[key] == 'ascend' ? 'asc' : 'desc';
      params.orderBy = key + "," + dir
    }
  }


  return new Promise((resolve, reject) => {
    get(url, params).then(pageable => {
      // 按pro table 的格式修改数据结构
      pageable.data = pageable.content;
      pageable.success = true;
      pageable.total = pageable.totalElements;
      resolve(pageable)
    }).catch(e => {
      reject(e)
    })
  })
}
