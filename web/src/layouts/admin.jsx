import React from 'react';
import {Layout, Menu, message} from 'antd';
import {history} from 'umi';
import {
  AppstoreOutlined, BankOutlined,
  CloudServerOutlined, ClusterOutlined, CreditCardOutlined,
  DatabaseOutlined, FileOutlined, LogoutOutlined, ProfileOutlined,
  ProjectOutlined,
  UserOutlined
} from "@ant-design/icons";
import {get} from "../utils/request";
import {notPermitted} from "../utils/SysConfig";

const {Content, Sider} = Layout;


export default class extends React.Component {

  state = {
    key: '/',
  };

  componentDidMount() {
    const loc = this.props.location;
    const {pathname} = loc
    const key = pathname.substr(1)

    this.setState({key})
  }

  logout = () => {
    localStorage.clear();
    const hide = message.loading("注销登录...", 0)
    get("/api/logout").then(rs => {
      history.push("/login")
      hide()
    })

  };

  render() {
    // 权限过滤
    let items = [
      {
        type: 'group', // Must have
        label: '交付中心',
        children: [
          {
            icon: <DatabaseOutlined style={{color: 'darkcyan'}}/>,
            iconColor: 'darkcyan',
            key: 'repository',
            label: '镜像',
            disabled: notPermitted('repository:list')
          },
          {
            key: 'dockerHub',
            label: '公共镜像',
            icon: <CloudServerOutlined style={{color: 'darkgoldenrod'}}/>
          },
        ]
      },
      {
        type: 'group', // Must have
        label: '应用平台',
        children: [
          {
            key: 'project',
            label: '项目',
            disabled: notPermitted('project:list'),

            icon: <ProjectOutlined style={{color: 'green'}}/>
          },
          {
            key: 'app',
            label: '应用',
            icon: <AppstoreOutlined style={{color: 'gold'}}/>,
            disabled: notPermitted('app:list'),
          },
          {
            key: 'host',
            label: '主机',
            icon: <ClusterOutlined style={{color: 'darkmagenta'}}/>,
            disabled: notPermitted('host:list'),
          },
        ]
      },



      {
        type: 'group',
        label: '其他',
        children: [


          {
            key: 'script',
            label: '脚本',
            icon: <ProfileOutlined style={{color: 'mediumvioletred'}}/>,
            disabled: notPermitted('script:list'),
          },
          {
            key: 'user',
            label: '用户',
            icon: <UserOutlined style={{color: 'burlywood'}}/>,
            disabled: notPermitted('user:list'),
          },
          {
            key: 'tenant',
            label: '租户',
            icon: <BankOutlined style={{color: 'gainsboro'}}/>,
            disabled: notPermitted('tenant:list'),
          },
          {
            key: 'gitCredential',
            label: '凭据',
            icon: <CreditCardOutlined style={{color: 'indianred'}}/>,
            disabled: notPermitted('gitCredential:list'),
          },

          {
            key: 'oplog',
            label: '日志',
            icon: <FileOutlined style={{color: 'darkorange'}}/>,
            disabled: notPermitted('oplog:list'),
          },
        ]
      },

      {
        key: 'logout',
        label: '退出',
        icon: <LogoutOutlined style={{color: 'rebeccapurple'}}/>
      },
    ]


    // 过滤disabled
    for (let item of items) {
      let list = item.children;
      if(list){
        list = list.filter(i => !(i.disabled === true))
        item.children = list
        if(list.length === 0 ){
          item.disabled = true;
        }
      }
    }
    items = items.filter(i=> !(i.disabled === true))


    return (
      <Layout style={{minHeight: 'calc(100vh - 20px)'}}>
        <Sider width={150}>

          <div className='py-3 m-4  bg-gray-800 flex align-middle justify-center '>
            <a className='text-white' onClick={() => history.push('/')}>HOME</a>
          </div>
          <Menu theme='dark'
                onClick={this.onClick} selectedKeys={[this.state.key]} items={items}/>
        </Sider>

        <Content className='pl-2'>
          {this.props.children}
        </Content>
      </Layout>
    );

  }

  onClick = ({key}) => {
    this.setState({key})
    if (key == 'logout') {
      this.logout()
      return
    }
    history.push('/' + key)
  }


}


