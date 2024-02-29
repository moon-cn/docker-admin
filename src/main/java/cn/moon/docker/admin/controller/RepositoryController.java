package cn.moon.docker.admin.controller;

import cn.moon.base.Option;
import cn.moon.docker.admin.entity.ImageTag;
import cn.moon.docker.admin.entity.Repository;
import cn.moon.docker.admin.service.RepositoryService;
import cn.hutool.core.util.StrUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("api/repository")
@RestController
public class RepositoryController {

    @Resource
    RepositoryService repositoryService;




    @RequiresPermissions("repository:list")
    @RequestMapping("list")
    public Page<Repository> list(@PageableDefault(direction = Sort.Direction.DESC, sort = "time") Pageable pageable, String keyword) throws Exception {
        if(keyword!= null){
            keyword = "%" + keyword + "%";
            return repositoryService.findByKeyword(pageable, keyword);
        }
        return repositoryService.findAll(pageable);
    }

    @RequestMapping("tagList")
    public List<ImageTag> tagList(String url) throws Exception {
        if (StrUtil.isEmpty(url) || url.equals("undefined")) {
            return Collections.emptyList();
        }

        List<ImageTag> tagList= repositoryService.findTagsByUrl(url);
        return tagList;
    }

    @RequestMapping("options")
    public List<Option> options() throws Exception {
        List<Repository> list = repositoryService.findAll(Sort.by(Sort.Direction.DESC, "time"));

        List<Option> options = list.stream().map(r -> new Option(r.getUrl(), r.getUrl())).collect(Collectors.toList());

        return options;
    }

    @RequestMapping("tagOptions")
    public Collection<Option> tagOptions(String url) {
        if (StrUtil.isEmpty(url) || url.equals("undefined")) {
            return Collections.emptyList();
        }

        List<ImageTag> tagList= repositoryService.findTagsByUrl(url);

        Set<Option> options = tagList.stream().map(r -> new Option(r.getName(), r.getName())).collect(Collectors.toSet());


        return options;
    }
}
