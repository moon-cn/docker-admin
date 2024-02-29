package cn.moon.docker.admin.service;

import cn.moon.base.BaseService;
import cn.moon.base.tool.YamlTool;
import cn.moon.docker.admin.BuildSuccessEvent;
import cn.moon.docker.admin.dao.DeployLogDao;
import cn.moon.docker.admin.entity.App;
import cn.moon.docker.admin.entity.BuildLog;
import cn.moon.docker.admin.entity.DeployLog;
import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.core.DockerManager;
import cn.moon.docker.core.callback.PullImageCallback;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Service
@Slf4j
public class AppService extends BaseService<App> {

    @Resource
    HostService hostService;

    @Resource
    DeployLogDao deployLogDao;

    @Resource
    DockerManager dockerManager;


    @Async
    public void deploy(App app) {
        MDC.put("logFileId", app.getId());
        // 修改更新时间
        app.setModifyTime(new Date());
        this.save(app);

        DeployLog deployLog = new DeployLog();
        deployLog.setAppId(app.getId());
        deployLog.setAppName(app.getName());

        deployLog = deployLogDao.save(deployLog);

        try {


            log.info("部署阶段开始");
            DockerClient client = dockerManager.getClient(app.getHost(), app.getImageUrl());


            String image = app.getImageUrl() + ":" + app.getImageTag();
            log.info("开始拉取镜像 {}", image);
            client.pullImageCmd(image).exec(new PullImageCallback(app.getId())).awaitCompletion();


            log.info("开始部署镜像 {}", image);
            App.AppConfig cfg = app.getConfig();


            List<Container> containers = getContainer(app.getName(), client);


            for (Container container : containers) {

                log.info("容器状态 {}", container.getState());
                if (container.getState().equals("running")) {
                    log.info("停止容器{}", container.getNames());
                    client.stopContainerCmd(container.getId()).exec();
                }
                log.info("删除容器{}", container.getNames());
                client.removeContainerCmd(container.getId()).exec();
            }


            HostConfig hostConfig = new HostConfig();

            // 网络、 端口
            List<ExposedPort> exposedPorts = new ArrayList<>();
            {
                if (StrUtil.isNotBlank(cfg.getNetworkMode())) {
                    hostConfig.withNetworkMode(cfg.getNetworkMode());
                }

                // 仅桥接模式下使用端口映射
                if (StrUtil.isBlank(cfg.getNetworkMode()) || cfg.getNetworkMode().equals("bridge")) {
                    Ports ports = new Ports();

                    if (cfg.getPorts() != null) {
                        for (App.PortBinding p : cfg.getPorts()) {
                            String protocol = p.getProtocol();
                            if (protocol == null) {
                                protocol = "TCP";
                            }
                            ExposedPort e = new ExposedPort(p.getPrivatePort(), InternetProtocol.valueOf(protocol));
                            exposedPorts.add(e);

                            ports.bind(e, Ports.Binding.bindPort(p.getPublicPort()));
                        }
                    }

                    hostConfig.withPortBindings(ports);
                }

            }


            // 路径绑定
            List<Bind> binds = new ArrayList<>();

            for (App.BindConfig v : cfg.getBinds()) {
                // /host:/container:ro
                binds.add(new Bind(v.getPublicVolume(), new Volume(v.getPrivateVolume()), AccessMode.rw));
            }
            hostConfig.withBinds(binds);


            // 环境变量
            Map<String, Object> dict = YamlTool.yamlToFlattenedMap(cfg.getEnvironmentYAML());
            List<String> envs = new ArrayList<>();
            for (Map.Entry<String, Object> e : dict.entrySet()) {
                envs.add(e.getKey() + "=" + e.getValue());
            }


            // 是否自动启动
            log.info("是否自动重启 {}", cfg.isRestart());
            if (cfg.isRestart()) {
                hostConfig.withRestartPolicy(RestartPolicy.alwaysRestart());
            }

            hostConfig.withPrivileged(true);


            // hosts，ip域名映射, 支持两种格式， 1. ip 域名 2.域名:ip
            String hosts = cfg.getExtraHosts();
            if (StrUtil.isNotBlank(hosts)) {
                List<String> list = StrUtil.splitTrim(hosts, " ");
                log.info("hosts={}", list);
                hostConfig.withExtraHosts(list.toArray(new String[list.size()]));
            }


            // cpu，限制

            // 日志限制
            LogConfig logConfig = new LogConfig(LogConfig.LoggingType.DEFAULT, new HashMap<>());
            logConfig.getConfig().put("max-size", "200m");
            hostConfig.withLogConfig(logConfig);


            log.info("主机配置{}", hostConfig.getBinds());
            CreateContainerCmd containerCmd = client.createContainerCmd(image);
            containerCmd
                    .withName(app.getName() + "_1")
                    .withLabels(dockerManager.getAppLabelFilter(app.getName()))
                    .withHostConfig(hostConfig)
                    .withExposedPorts(exposedPorts) // 如果dockerfile中未指定端口，需要在这里指定
                    .withEnv(envs);

            String cmd = app.getConfig().getCmd();
            if (StrUtil.isNotEmpty(cmd)) {
                List<String> cmds = StrUtil.splitTrim(cmd, " ");
                containerCmd.withCmd(cmds);
            }


            CreateContainerResponse response = containerCmd.exec();


            log.info("创建容器{}", response);

            String containerId = response.getId();

            client.startContainerCmd(containerId).exec();


            log.info("启动容器");
            log.info("部署阶段结束");
        } catch (Exception e) {
            log.info("--------------------------------------------------");
            log.info("部署失败:" + e.getClass().getName() + "=>" + e.getMessage());


            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));

            String exStr = writer.toString();
            log.info(exStr);


            e.printStackTrace();
            log.info("--------------------------------------------------");
        }

    }

    public void stop(String id) {
        App app = this.findOne(id);


        DockerClient client = dockerManager.getClient(app.getHost());

        List<Container> list = getContainer(app.getName(), client);

        for (Container container : list) {
            client.stopContainerCmd(container.getId()).exec();
        }
    }


    public void start(String id) {
        App app = this.findOne(id);

        DockerClient client = dockerManager.getClient(app.getHost());

        List<Container> list = getContainer(app.getName(), client);

        for (Container container : list) {
            client.startContainerCmd(container.getId()).exec();
        }
    }

    public App rename(String appId, String newName) {
        App app = this.findOne(appId);
        Assert.notNull(app, "app不存在");
        //判断名字是否相同进行部署
        if (newName.equals(app.getName())) {
            return app;
        }
        this.deleteContainer(app);
        app.setName(newName);
        App saved = this.save(app);

        this.deploy(app);

        return saved;
    }

    private List<Container> getContainer(String name, DockerClient client) {
        Map<String, String> labels = dockerManager.getAppLabelFilter(name);
        List<Container> list = client.listContainersCmd()
                .withLabelFilter(labels)
                .withShowAll(true).exec();
        return list;
    }

    public Container getContainer(App app) {
        DockerClient client = dockerManager.getClient(app.getHost());

        String name = app.getName();

        Map<String, String> labels = dockerManager.getAppLabelFilter(name);
        List<Container> list = client.listContainersCmd().withLabelFilter(labels).withShowAll(true).exec();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public void deleteApp(String id) {
        // 远程删除应用
        App app = this.findOne(id);
        deleteContainer(app);

        baseDao.deleteById(id);
    }

    public void moveApp(String id, String hostId) {
        Assert.hasLength(hostId, "hostId不能为空");
        // 远程删除应用
        App app = this.findOne(id);
        this.deleteContainer(app);

        Host host = hostService.findOne(hostId);

        app.setHost(host);
        app = this.save(app);

        SpringUtil.getBean(getClass()).deploy(app);
    }

    public void updateAppVersion(String id, String tag) {
        Assert.hasLength(tag, "tag不能为空");
        // 远程删除应用
        App app = this.findOne(id);
        this.deleteContainer(app);

        app.setImageTag(tag);
        save(app);

        SpringUtil.getBean(getClass()).deploy(app);
    }

    private void deleteContainer(App app) {
        DockerClient client = dockerManager.getClient(app.getHost());


        Map<String, String> labels = dockerManager.getAppLabelFilter(app.getName());
        List<Container> list = client.listContainersCmd().withLabelFilter(labels).withShowAll(true).exec();
        log.info("已有容器个数 {}", list.size());

        list.forEach(c -> {
            if (c.getState().equals("running")) {
                log.info("正在停止容器 {}", c);
                client.stopContainerCmd(c.getId()).exec();
            }
            log.info("正在删除容器 {}", c);
            client.removeContainerCmd(c.getId()).exec();
        });
    }

    public App updateConfig(String id, App.AppConfig appConfig) {
        App app = this.findOne(id);
        app.setConfig(appConfig);

        app = this.save(app);
        return app;
    }

    @EventListener
    public void onBuildSuccess(BuildSuccessEvent event) {
        log.info("构建成功，开始检测需自动部署的应用");
        BuildLog buildLog = event.getBuildLog();

        // 自动部署
        List<App> list = this.findAll();

        // 让注解生效
        AppService $this = SpringUtil.getBean(getClass());

        for (App app : list) {
            if (app.getAutoDeploy() != null && app.getAutoDeploy() && app.getImageUrl().equals(buildLog.getImageUrl())) {
                app.setImageTag(event.getVersion());
                $this.deploy(app);
            }
        }
    }
}
