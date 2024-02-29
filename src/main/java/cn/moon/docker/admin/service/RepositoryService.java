package cn.moon.docker.admin.service;

import cn.moon.base.BaseService;
import cn.moon.docker.admin.BuildSuccessEvent;
import cn.moon.docker.admin.dao.ImageTagDao;
import cn.moon.docker.admin.dao.ProjectDao;
import cn.moon.docker.admin.dao.RepositoryDao;
import cn.moon.docker.admin.entity.ImageTag;
import cn.moon.docker.admin.entity.Project;
import cn.moon.docker.admin.entity.Repository;
import cn.moon.docker.core.api.RegistryApi;
import cn.moon.docker.core.api.RegistryConfig;
import cn.moon.docker.core.api.RepositoryVo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class RepositoryService extends BaseService<Repository> {

    @Resource
    RepositoryDao repositoryDao;

    @Resource
    RegistryConfig registryConfig;

    @Resource
    ProjectDao projectDao;

    @Resource
    ImageTagDao imageTagDao;

    public Page<Repository> findByKeyword(Pageable pageable, String url) {
        return repositoryDao.findByUrlLike(pageable, url);
    }

    public Repository findByUrl(String url) {
        return repositoryDao.findByUrl(url);
    }

    @Transactional
    public List<ImageTag> findTagsByUrl(String url) {
        Repository repository = repositoryDao.findByUrl(url);

        log.error("镜像仓库未找到" + url);
        if(repository == null){
            return Collections.emptyList();
        }


        List<ImageTag> tags =imageTagDao.findByUrlOrderByTime(url);

        return tags;
    }

    @Transactional
    public void saveTags( List<ImageTag> tagList) {
        imageTagDao.saveAll(tagList);
    }

    @Async
    @EventListener(BuildSuccessEvent.class)
    @Transactional
    public void sysByProject(BuildSuccessEvent e) throws Exception {
        log.info("监听到收到项目构建成功 , 开始同步远程仓库相关信息{}", e);
        String url = e.getBuildLog().getImageUrl();

        String projectId = e.getBuildLog().getProjectId();
        Project project = projectDao.findById(projectId).orElse(null);


        RegistryApi api = registryConfig.findApiByUrl(url);
        Page<RepositoryVo> page = api.findRepositoryList(Pageable.ofSize(50), project.getName());

        RepositoryVo vo = page.stream().filter(t -> t.getName().equals(project.getName())).findFirst().orElse(null);

        Assert.notNull(vo, "远程仓库未找到" + project.getName());

        Repository repository = repositoryDao.findByUrl(vo.getUrl());
        if(repository == null){
            repository = new Repository();
            BeanUtil.copyProperties(vo, repository);
        }

        repository.setTenantId(project.getTenantId());
        repositoryDao.save(repository);
    }

    @Async
    @EventListener(BuildSuccessEvent.class)
    @Transactional
    public void addTag(BuildSuccessEvent e) {
        log.info("监听到收到项目构建成功 ，修改本地仓库...{}", e);
        String url = e.getBuildLog().getImageUrl();
        String version = e.getBuildLog().getVersion();

        long count = imageTagDao.countByUrlAndName(url, version);
        if(count == 0){
            ImageTag tag = new ImageTag();
            tag.setName(version);
            tag.setUrl(url);
            tag.setTime(new Date());
            imageTagDao.save(tag);
            log.info("添加tag 成功{}", tag);
        }

    }
}
