package cn.moon.docker.admin;

import cn.moon.docker.admin.dao.ProjectDao;
import cn.moon.docker.admin.entity.ImageTag;
import cn.moon.docker.admin.entity.Project;
import cn.moon.docker.admin.entity.Repository;
import cn.moon.docker.admin.service.RepositoryService;
import cn.moon.docker.core.api.RegistryApi;
import cn.moon.docker.core.api.RegistryConfig;
import cn.moon.docker.core.api.RepositoryVo;
import cn.moon.docker.core.api.TagVo;
import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SyncRepositoryJob {

    @Resource
    RegistryConfig config;

    @Resource
    RepositoryService repositoryService;

    @Resource
    ProjectDao projectDao;

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.HOURS, initialDelay = 1000 * 60 * 60 * 2)
    public void sync() throws Exception {
        log.info("同步镜像服务");
        List<Repository> all = repositoryService.findAll();
        List<String> allUrls = all.stream().map(Repository::getUrl).collect(Collectors.toList());
        for (RegistryConfig.Registry registry : config.getList()) {
            log.info("注册中心 {}", registry.getUrl());
            RegistryApi api = registry.createApi();

            Pageable pageable = Pageable.ofSize(50);
            Page<RepositoryVo> page;
            do {
                log.info("获取数据 {}", pageable);
                page = api.findRepositoryList(pageable, null);
                pageable = page.nextPageable();

                for (RepositoryVo vo : page) {
                    if (allUrls.contains(vo.getUrl())) {
                        // 存在，不同步
                        continue;
                    }

                    log.info("同步 {}", vo.getName());
                    Repository repository = new Repository();
                    BeanUtil.copyProperties(vo, repository);


                    repositoryService.save(repository);
                }
                Thread.sleep(100);
            } while (page != null && page.hasNext());
        }

        // 同步租户
        for (Repository repository : all) {

            Project project = projectDao.findByName(repository.getName());
            if (project != null) {
                repository.setTenantId(project.getTenantId());
                repositoryService.save(repository);
            }
        }


        log.info("同步镜像服务结束");
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.HOURS, initialDelay = 1000 * 60 * 60 * 2)
    public void syncTag() throws Exception {
        List<Repository> list = repositoryService.findAll();

        for (Repository repository : list) {
            String url = repository.getUrl();
            RegistryApi api = config.findApiByUrl(url);

            Pageable pageable = Pageable.ofSize(50);
            Page<TagVo> page;
            do {
                log.info("同步tag {}", url, pageable);
                page = api.findTagList(url, pageable);
                pageable = page.nextPageable();

                List<ImageTag> tagList = page.stream().map(vo -> {
                    ImageTag tag = new ImageTag();
                    BeanUtil.copyProperties(vo, tag);

                    return tag;
                }).collect(Collectors.toList());


                repositoryService.saveTags(tagList);

                Thread.sleep(100);
            } while (page != null && page.hasNext());
        }


    }

}
