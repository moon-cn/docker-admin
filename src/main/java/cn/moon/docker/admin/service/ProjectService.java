package cn.moon.docker.admin.service;

import cn.moon.base.BaseService;
import cn.moon.base.tool.GitTool;
import cn.moon.docker.admin.BuildSuccessEvent;
import cn.moon.docker.admin.dao.BuildLogDao;
import cn.moon.docker.admin.entity.BuildLog;
import cn.moon.docker.admin.entity.GitCredential;
import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.admin.entity.Project;
import cn.moon.docker.core.DockerManager;
import cn.moon.docker.core.api.RegistryConfig;
import cn.moon.docker.core.callback.MyBuildImageResultCallback;
import cn.moon.docker.core.callback.MyPushImageCallback;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PushImageCmd;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ProjectService extends BaseService<Project> {

    @Resource
    BuildLogDao logDao;


    @Resource
    RegistryConfig cfg;


    @Resource
    HostService hostService;

    @Resource
    DockerManager dockerService;

    @Resource
    GitCredentialService gitCredentialService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private final Map<String, MyBuildImageResultCallback> buildThreadMap = new HashMap<>();


    public void stopBuild(String id) throws IOException {
        MyBuildImageResultCallback callback = buildThreadMap.get(id);
        if (callback != null) {
            callback.close();
        }

        BuildLog buildLog = logDao.findById(id).get();

        buildLog.setSuccess(false);
        buildLog.setCompleteTime(new Date());
        buildLog.setTimeSpend(buildLog.getCompleteTime().getTime() - buildLog.getCreateTime().getTime());
        logDao.save(buildLog);
    }


    @Async
    public void buildImage(String buildlogFileId, String branchOrTag, String version, String context, String dockerfile, boolean useCache) {
        MDC.put("logFileId", buildlogFileId);
        BuildLog buildLog = logDao.findById(buildlogFileId).orElseGet(null);
        Project project = this.findOne(buildLog.getProjectId());
        try {

            log.info("日志ID {}", buildLog.getId());
            log.info("开始构建镜像任务 {} {} {}", project.getName(), branchOrTag, version);

            Host host = hostService.getDefaultDockerRunner();
            Assert.notNull(host, "无构建主机");
            log.info("构建主机： {} {} {}", host.getName(), host.getDockerHost(), host.getRemark());

            String username = null;
            String password = null;
            GitCredential credential = gitCredentialService.findBestByUrl(project.getGitUrl());
            if (credential != null) {
                username = credential.getUsername();
                password = credential.getPassword();
            }

            GitTool.CloneResult cloneResult = GitTool.clone(project.getGitUrl(), username, password, branchOrTag);
            File workDir = cloneResult.getDir();
            log.info("代码下载完毕 " + workDir);
            log.info("提交信息:" + cloneResult.getCodeMessage());

            log.info("dockerfile {}", dockerfile);


            buildLog.setBuildHostName(host.getName());
            buildLog.setCodeMessage(cloneResult.getCodeMessage());
            buildLog = logDao.save(buildLog);


            RegistryConfig.Registry registry = cfg.findDefault();

            DockerClient client = dockerService.getClient(host, registry);


            String imageUrl = registry.getUrl() + "/" + registry.getNamespace() + "/" + project.getName();
            String image = imageUrl + ":" + version;
            log.info("发布镜像url {}", image);

            Assert.state(!StrUtil.containsBlank(imageUrl), "镜像路径不能包含空格");


            buildLog.setImageUrl(imageUrl);


            Set<String> tags = Sets.newHashSet(image);

            File buildDir = new File(workDir, context);


            log.info("向docker发送构建指令");
            MyBuildImageResultCallback buildCallback = new MyBuildImageResultCallback(buildlogFileId);
            buildThreadMap.put(buildLog.getId(), buildCallback);
            client.buildImageCmd(buildDir)


                    // 删除构建产生的容器
                    .withForcerm(true)

                    .withNetworkMode("host")
                    .withTags(tags)
                    .withNoCache(!useCache)
                    .withDockerfilePath(dockerfile)
                    .exec(buildCallback).awaitImageId();
            log.info("镜像构建结束 ");

            buildThreadMap.remove(buildLog.getId());

            // 推送
            log.info("推送镜像");
            for (String tag : tags) {
                PushImageCmd pushImageCmd = client.pushImageCmd(tag);
                MyPushImageCallback callback = pushImageCmd.exec(new MyPushImageCallback(buildlogFileId));
                callback.awaitCompletion();
                log.info("推送镜像结束 {}", tag);
            }

            client.close();


            buildLog.setSuccess(true);
            buildLog.setCompleteTime(new Date());
            buildLog.setTimeSpend(buildLog.getCompleteTime().getTime() - buildLog.getCreateTime().getTime());
            buildLog = logDao.save(buildLog);
            log.info("已更新构建日志{}", buildLog);


            Map<String, Object> data = BeanUtil.beanToMap(project, "id", "name", "value");

            BuildSuccessEvent event = new BuildSuccessEvent(this);
            event.setData(data);
            event.setBuildLog(buildLog);
            event.setVersion(version);

            applicationEventPublisher.publishEvent(event);
            log.info("已抛出事件{}", event);

            log.info("构建阶段结束");
        } catch (Exception e) {
            log.info("异常 {} {}", e.getClass().getName(), e.getMessage());


            if (e instanceof IllegalArgumentException && e.getMessage() != null && e.getMessage().contains("Dockerfile does not exist")) {
                log.info("请确保项目下至少有一个Dockerfile文件，不论是否指定其他Dockerfile");
            }
            e.printStackTrace();

            buildLog.setSuccess(false);
            buildLog.setCompleteTime(new Date());
            buildLog.setTimeSpend(buildLog.getCompleteTime().getTime() - buildLog.getCreateTime().getTime());
            logDao.save(buildLog);
        }

        MDC.remove("logFileId");
    }


    @Transactional
    public void deleteProject(String id) {
        BuildLog buildLog = new BuildLog();
        buildLog.setProjectId(id);
        List<BuildLog> logList = logDao.findAll(Example.of(buildLog));

        logDao.deleteInBatch(logList);

        baseDao.deleteById(id);
    }

    @Transactional
    public Project saveProject(Project project) {
        project = baseDao.save(project);
        return project;
    }

    @Transactional
    public void cleanErrorLog(String id) {
        BuildLog buildLog = new BuildLog();
        buildLog.setProjectId(id);
        buildLog.setSuccess(false);
        List<BuildLog> logList = logDao.findAll(Example.of(buildLog));

        logDao.deleteAllInBatch(logList);
    }
}
