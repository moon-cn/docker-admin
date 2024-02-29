export const site = {
  title: '容器管理平台',
  serverUrl: 'http://127.0.0.1:7001/'
}

let loginData = {};
export function setLogin(data){
    loginData = data;
}

export function getLoginData(){
  return loginData;
}

export function isPermitted(p){
 return  loginData.perms.indexOf(p) != -1
}
export function notPermitted(p){
  return  !isPermitted(p)
}
