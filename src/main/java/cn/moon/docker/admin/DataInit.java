package cn.moon.docker.admin;

import cn.moon.docker.admin.entity.Host;
import cn.moon.base.user.User;
import cn.moon.docker.admin.service.HostService;
import cn.moon.base.user.UserService;
import cn.moon.base.role.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 启动后执行
 */
@Component
@Slf4j
public class DataInit implements ApplicationRunner {


    @Resource
    UserService userService;

    @Resource
    HostService hostService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if( userService.count() == 0){
            User user = new User();
            user.setUsername("admin");
            user.setPassword("123456");
            user.setRole(Role.admin);
            userService.save(user);

            log.info("创建默认账号 {}", user);
        }

        if(hostService.count() == 0){
            Host host = new Host();
            host.setName("宿主机");
            host.setDockerHost("unix:///var/run/docker.sock");
            host.setIsRunner(true);
            hostService.save(host);
            log.info("创建默认主机配置 {}", host);
        }


    }
}
