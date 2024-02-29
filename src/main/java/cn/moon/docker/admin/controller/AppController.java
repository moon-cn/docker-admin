package cn.moon.docker.admin.controller;

import cn.moon.base.Option;
import cn.moon.docker.admin.service.HostService;
import cn.moon.docker.admin.bean.ContainerVo;
import cn.moon.docker.admin.entity.App;
import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.admin.service.AppService;
import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import com.aliyuncs.exceptions.ClientException;
import com.github.dockerjava.api.model.Container;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "api/app")
public class AppController {


    @Resource
    private AppService service;

    @Resource
    private HostService hostService;




    @RequiresPermissions("app:list")
    @RequestMapping("list")
    public Page<App> list(String keyword, @PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable, HttpSession session) {
        App app = new App();
        app.setName(keyword);


        ExampleMatcher matcher = ExampleMatcher.matchingAny()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<App> example = Example.of(app, matcher);


        Page<App> list = service.findAll(example, pageable);
        return list;
    }

    @RequestMapping("get")
    public App view(String id) {
        return service.findOne(id);
    }

    @RequestMapping("container")
    public AjaxResult container(String id) {
        App app = service.findOne(id);
        Assert.state(app != null,"应用不存在");
        Container container = service.getContainer(app);
        Assert.state(container != null,"容器部署中...");

        return AjaxResult.success("获取容器信息成功", new ContainerVo(container)) ;
    }


    @RequiresPermissions("app:save")
    @RequestMapping("save")
    public AjaxResult save(@RequestBody App app) {
        app.setConfig(new App.AppConfig());

        Host host = hostService.findOne(app.getHostId());
        app.setHost(host);
        App db = service.save(app);


        service.deploy(db);

        return AjaxResult.success("部署指令已发送", db.getId());
    }

    @RequiresPermissions("app:save")
    @RequestMapping("update")
    public AjaxResult update(@RequestBody App project) {
        service.save(project);
        return AjaxResult.success("修改成功");
    }

    @RequiresPermissions("app:config")
    @RequestMapping("updateConfig")
    public AjaxResult updateConfig(String id, @RequestBody App.AppConfig appConfig) {
        App app = service.updateConfig(id, appConfig);
        service.deploy(app);

        return AjaxResult.success("修改成功，应用会自动重启", app);
    }

    @RequiresPermissions("app:update")
    @RequestMapping("updateVersion")
    public AjaxResult updateVersion(String id, String version) {
        service.updateAppVersion(id, version);

        return AjaxResult.success("更新指定已发布");
    }


    @RequiresPermissions("app:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id, Boolean force) throws ClientException {
        try {
            service.deleteApp(id);
        } catch (Exception e) {
            if (force != null && force) {
                service.deleteById(id);
                return AjaxResult.success();
            }
            return AjaxResult.error("删除失败");
        }


        return AjaxResult.success();
    }




    @RequiresPermissions("app:deploy")
    @RequestMapping("deploy/{id}")
    public AjaxResult deploy(@PathVariable String id) {
        log.info("开始部署");
        App app = service.findOne(id);

        service.deploy(app);
        log.info("部署指令已发送");
        return AjaxResult.success();
    }


    @RequiresPermissions("app:deploy")
    @RequestMapping("autoDeploy")
    public AjaxResult autoDeploy(String id, boolean autoDeploy) throws InterruptedException {

        App db = service.findOne(id);
        db.setAutoDeploy(autoDeploy);

        service.save(db);


        AjaxResult rs = AjaxResult.success("自动部署:" + (autoDeploy ? "启用" : "停用"));
        return rs;
    }


    @RequiresPermissions("app:start")
    @RequestMapping("autoRestart")
    public AjaxResult autoRestart(String id, boolean autoRestart) throws InterruptedException {

        App db = service.findOne(id);
        db.setAutoRestart(autoRestart);
        db.getConfig().setRestart(autoRestart);

        service.save(db);
        service.deploy(db);


        AjaxResult rs = AjaxResult.success("自动重启:" + (autoRestart ? "启用" : "停用"));
        return rs;
    }

    @RequiresPermissions("app:moveApp")
    @RequestMapping("moveApp")
    public AjaxResult moveApp(String id, String hostId) {
        service.moveApp(id, hostId);

        return AjaxResult.success();
    }

    @RequiresPermissions("app:start")
    @RequestMapping("start/{appId}")
    public AjaxResult start(@PathVariable String appId) {
        service.start(appId);
        return AjaxResult.success("启动指令已发送");
    }

    @RequiresPermissions("app:stop")
    @RequestMapping("stop/{appId}")
    public AjaxResult stop(@PathVariable String appId) {
        service.stop(appId);
        return AjaxResult.success("停止指令已发送");
    }

    @RequiresPermissions("app:config")
    @RequestMapping("rename")
    public AjaxResult rename(@RequestBody Map<String,String> map) {
        String appId = map.get("appId");
        String newName = map.get("newName");
        Assert.hasText(appId,"appId不能为空");
        Assert.hasText(newName, "新名称不能为空");
        App app = service.rename(appId, newName);


        return AjaxResult.success("部署指令已发送", app);
    }


    @RequestMapping("options")
    public AjaxResult options() {
        List<Option> list = new ArrayList<>();
        Sort sort = Sort.by(Sort.Direction.DESC, BaseEntity.Fields.modifyTime);

        List<App> all = service.findAll(sort);

        for (App app : all) {
            list.add(new Option(app.getId(), app.getName()));
        }

        return AjaxResult.success(null, list);
    }
}
