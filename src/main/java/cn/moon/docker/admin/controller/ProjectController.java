package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.entity.BuildLog;
import cn.moon.docker.admin.entity.Project;
import cn.moon.docker.admin.service.BuildLogService;
import cn.moon.docker.admin.service.ProjectService;
import cn.moon.base.Option;
import cn.hutool.core.util.StrUtil;
import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.MDC;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "api/project")
public class ProjectController {


    @Resource
    private ProjectService service;

    @Resource
    private BuildLogService logService;



    @RequiresPermissions("project:list")
    @RequestMapping("list")
    public Page<Project> list(String keyword, @PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable) {


        Project project = new Project();
        project.setName(keyword);



        ExampleMatcher matcher = ExampleMatcher.matchingAny()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Project> example = Example.of(project, matcher);


        Page<Project> list = service.findAll(example, pageable);
        return list;
    }

    @RequiresPermissions("project:save")
    @RequestMapping("save")
    public AjaxResult save(@RequestBody Project project) {
        Project db = service.saveProject(project);

        AjaxResult rs = AjaxResult.success("保存成功",null);
        return rs;
    }

    @RequiresPermissions("project:save")
    @RequestMapping("update")
    public AjaxResult update(@RequestBody Project project) {
        service.save(project);
        return AjaxResult.success("修改成功",null);
    }



    @RequiresPermissions("project:delete")
    @RequestMapping("delete")
    public AjaxResult delete( String id) throws ClientException {
        Project project = service.findOne(id);
        service.deleteProject(id);

        return AjaxResult.success("删除成功",null);
    }


    @RequiresPermissions("project:list")
    @RequestMapping("get")
    public Project get(String id) {
        return service.findOne(id);
    }


    @RequiresPermissions("project:build")
    @RequestMapping("build")
    public AjaxResult build(@RequestParam String projectId,
                            String value,
                            String version,
                            @RequestParam(defaultValue = "/") String context,
                            @RequestParam(defaultValue = "Dockerfile") String dockerfile,
                            @RequestParam(defaultValue = "true") Boolean useCache) throws InterruptedException, IOException, GitAPIException {

        Assert.notNull(value, "请输入branch或tag");
        if (StrUtil.isEmpty(version)) {
            version = value;
        }

        // 更新最近时间,方便排序
        Project project = service.findOne(projectId);
        project.setModifyTime(new Date());
        service.save(project);

        BuildLog buildLog = new BuildLog();
        buildLog.setProjectId(projectId);
        buildLog.setVersion(version);
        buildLog.setProjectName(project.getName());
        buildLog.setDockerfile(dockerfile);
        buildLog.setValue(value);
        buildLog = logService.save(buildLog);

        MDC.put("logFileId",buildLog.getId());
        log.info("控制层收到构建指令，开始异步调用服务");
        service.buildImage(buildLog.getId(), value, version, context, dockerfile, useCache);


        return AjaxResult.success();
    }

    @RequestMapping("stopBuild")
    public AjaxResult stopBuild(@RequestParam String id) throws IOException {
        service.stopBuild(id);

        return AjaxResult.success();
    }
    @RequestMapping("cleanErrorLog")
    public AjaxResult cleanErrorLog(@RequestParam String id) throws IOException {
        service.cleanErrorLog(id);
        return AjaxResult.success();
    }



    @RequestMapping("options")
    public List<Option> options() throws InterruptedException, IOException, GitAPIException {
        List<Project> list = service.findAll(Sort.by(Sort.Direction.DESC, BaseEntity.Fields.modifyTime));

        List<Option> options = new ArrayList<>();
        for (Project h : list) {
            options.add(new Option(h.getId(), h.getName()));
        }


        return options;
    }
}
