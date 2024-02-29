package cn.moon.base.shiro;

import cn.moon.base.user.UserDao;
import cn.moon.base.user.User;
import cn.moon.base.role.Role;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyRealm extends AuthorizingRealm {


    @Resource
    UserDao userDao;


    //用于在进行用户身份认证时获取用户的认证信息。
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String username = token.getUsername();
        String password = new String(token.getPassword());

        // 根据用户名查询用户信息（比如从数据库中查询）
        User user = userDao.findByUsername(username);

        // 判断用户是否存在
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }

        // 验证密码是否匹配
        if (!password.equals(user.getPassword())) {
            throw new IncorrectCredentialsException("密码错误");
        }

        // 构建认证信息
        CurrentUser userInfo = new CurrentUser();
        userInfo.setUsername(username);
        if(user.getTenant() != null){
            userInfo.setTenantId(user.getTenant().getId());

        }
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(userInfo, user.getPassword(), getName());


        return authenticationInfo;
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取用户名
        String username = ((CurrentUser) principals.getPrimaryPrincipal()).getUsername();

        User user = userDao.findByUsername(username);

        Role role = user.getRole();

        if(username.equals("admin") && role==null){
            role = Role.admin;
        }


        // 构建授权信息
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        for (String perm : role.getPerms()) {
            authorizationInfo.addStringPermission(perm);
        }

        authorizationInfo.addRole(role.name());



        return authorizationInfo;
    }
}
