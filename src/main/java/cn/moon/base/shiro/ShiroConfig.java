package cn.moon.base.shiro;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Map;


/**
 * @Shiro内置过滤器
 * anon         org.apache.shiro.web.filter.authc.AnonymousFilter
 * authc        org.apache.shiro.web.filter.authc.FormAuthenticationFilter
 * authcBasic   org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
 * perms        org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter
 * port         org.apache.shiro.web.filter.authz.PortFilter
 * rest         org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter
 * roles        org.apache.shiro.web.filter.authz.RolesAuthorizationFilter
 * ssl          org.apache.shiro.web.filter.authz.SslFilter
 * user         org.apache.shiro.web.filter.authc.UserFilter
 *
 */


@Configuration
public class ShiroConfig {

  @Resource
  MyRealm myRealm;




  @Bean
  public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager){
    ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();

    bean.getFilters().put("authc",new JsonFilter());

    bean.setSecurityManager(securityManager);

    Map<String, String> map = bean.getFilterChainDefinitionMap();

    map.put("/api/login/check", "anon");
    map.put("/api/login", "anon");
    map.put("/api/log/**", "anon");
    map.put("/api/**", "authc");
    map.put("/**", "anon");

    return bean;
  }

  @Bean
  public DefaultWebSecurityManager securityManager() {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    securityManager.setRealm(myRealm);
    return securityManager;
  }



}