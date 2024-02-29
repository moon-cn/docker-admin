package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.entity.GitCredential;
import cn.moon.docker.admin.service.GitCredentialService;
import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@Slf4j
@RequestMapping(value = "api/gitCredential")
public class GitCredentialController {


    @Resource
    private GitCredentialService service;


    @RequiresPermissions("gitCredential:list")
    @RequestMapping("list")
    public Page<GitCredential> list(String keyword, @PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable, HttpSession session) {


        GitCredential gitCredential = new GitCredential();



        ExampleMatcher matcher = ExampleMatcher.matchingAny()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<GitCredential> example = Example.of(gitCredential, matcher);


        Page<GitCredential> list = service.findAll(example, pageable);
        return list;
    }

    @RequiresPermissions("gitCredential:save")
    @RequestMapping("save")
    public AjaxResult save(@RequestBody GitCredential gitCredential) {
        GitCredential db = service.save(gitCredential);

        AjaxResult rs = AjaxResult.success("保存成功",null);
        return rs;
    }

    @RequiresPermissions("gitCredential:save")
    @RequestMapping("update")
    public AjaxResult update(@RequestBody GitCredential gitCredential) {
        service.save(gitCredential);
        return AjaxResult.success("修改成功",null);
    }



    @RequiresPermissions("gitCredential:delete")
    @RequestMapping("delete")
    public AjaxResult delete( String id) throws ClientException {
        service.deleteById(id);
        return AjaxResult.success("删除成功",null);
    }


 

    

  
}
