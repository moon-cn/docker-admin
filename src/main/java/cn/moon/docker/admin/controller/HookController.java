package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.entity.BuildLog;
import cn.moon.docker.admin.entity.Project;
import cn.moon.docker.admin.service.BuildLogService;
import cn.moon.docker.admin.service.ProjectService;
import cn.hutool.core.date.DateUtil;
import cn.moon.base.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("hook")
public class HookController {


    @Resource
    private ProjectService service;
    @Resource
    private BuildLogService logService;


    @RequestMapping("build/{id}")
    public AjaxResult build(@PathVariable String id) throws IOException, GitAPIException, InterruptedException {


        String version = DateUtil.today();

        // 更新最近时间,方便排序
        Project db = service.findOne(id);
        db.setModifyTime(new Date());
        service.save(db);

        BuildLog buildLog = new BuildLog();
        buildLog.setProjectId(id);
        buildLog.setVersion(version);
        buildLog.setProjectName(db.getName());
        buildLog.setDockerfile(db.getDockerfile());
        buildLog.setValue(db.getBranch());
        buildLog = logService.save(buildLog);



        service.buildImage(buildLog.getId(), buildLog.getValue(), version, "/", buildLog.getDockerfile(), true);

        return AjaxResult.success("构建命令已发送");
    }


}
