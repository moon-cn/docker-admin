package cn.moon.docker.admin.controller;

import cn.moon.docker.core.api.RepositoryVo;
import cn.moon.docker.core.api.impl.DockerHubApi;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/dockerHub")
public class DockerHubController  {

    @Resource
    DockerHubApi dockerHub;

    @GetMapping("list")
    public Page<RepositoryVo> list(String keyword) throws Exception {
        Page<RepositoryVo> list = dockerHub.findRepositoryList(null, keyword);
        return list;
    }




}
