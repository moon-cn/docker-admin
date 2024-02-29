package cn;


import cn.moon.base.user.UserService;
import cn.moon.docker.admin.service.ProjectService;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class ProjectTest {

    @Resource
    ProjectService projectService;

    @Resource
    UserService userService;





}
