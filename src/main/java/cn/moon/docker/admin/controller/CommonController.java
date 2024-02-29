package cn.moon.docker.admin.controller;

import cn.moon.base.AjaxResult;
import cn.moon.docker.admin.dao.AppDao;
import cn.moon.docker.admin.dao.HostDao;
import cn.moon.docker.admin.dao.ProjectDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api")
public class CommonController {

    @Resource
    AppDao appDao;

    @Resource
    ProjectDao projectDao;


    @Resource
    HostDao hostDao;


    @GetMapping("homeInfo")
    public AjaxResult homeInfo(){
        long count = appDao.count();
        long count1 = projectDao.count();
        long count2 = hostDao.count();

        Map<String,Object> data = new HashMap<>();
        data.put("appCount",count);
        data.put("projectCount", count1);
        data.put("hostCount", count2);

        return AjaxResult.success(null,data);
    }
}
