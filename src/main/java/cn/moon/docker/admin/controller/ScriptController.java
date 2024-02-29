package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.service.ScriptService;
import cn.moon.docker.admin.entity.Script;
import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping(value = "api/script")
public class ScriptController {

    @Resource
    ScriptService service;

    @RequiresPermissions("script:list")
    @RequestMapping("list")
    public Page<Script> list(String keyword, @PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable) {
        Script Script = new Script();
        Script.setName(keyword);

        ExampleMatcher matcher = ExampleMatcher.matchingAny()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Script> example = Example.of(Script, matcher);


        Page<Script> list = service.findAll(example, pageable);
        return list;
    }

    @RequestMapping("save")
    public AjaxResult save(@RequestBody Script Script) {
        Script db = service.save(Script);

        AjaxResult rs = AjaxResult.success("保存成功", db.getId());
        return rs;
    }

    @RequestMapping("update")
    public AjaxResult update(@RequestBody Script Script) {
        service.save(Script);
        return AjaxResult.success("修改成功");
    }

    @RequestMapping("delete")
    public AjaxResult delete( String id) throws ClientException {
        service.deleteById(id);
        return AjaxResult.success("删除成功");
    }

    @RequestMapping("get")
    public Script get(String id) {
        return service.findOne(id);
    }


    @RequestMapping("run")
    public AjaxResult run(@RequestParam String id, String value) throws InterruptedException, IOException, GitAPIException {

        Assert.notNull(value, "请输入branch或tag");


        service.run(id, value);
        return AjaxResult.success();
    }





}
