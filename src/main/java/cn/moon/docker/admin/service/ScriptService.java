package cn.moon.docker.admin.service;

import cn.moon.docker.admin.dao.ScriptLogDao;
import cn.moon.base.tool.GitTool;
import cn.moon.base.BaseService;
import cn.moon.docker.admin.entity.*;
import cn.moon.docker.core.DockerManager;
import cn.moon.docker.core.callback.MyBuildImageResultCallback;
import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ScriptService extends BaseService<Script> {

    @Resource
    HostService hostService;

    @Resource
    ScriptLogDao logDao;

    @Resource
    DockerManager dockerService;


    @Resource
    GitCredentialService gitCredentialService;





    @Async
    public void run(String id, String branchOrTag) {
        // 更新最近时间,方便排序
        Script script = this.findOne(id);
        script.setModifyTime(new Date());
        script =  this.save(script);


        ScriptLog scriptLog = new ScriptLog();
        scriptLog.setProjectId(script.getProject().getId());

        scriptLog.setScriptId(script.getId());
        scriptLog.setScriptName(script.getName());

        scriptLog.setProjectName(script.getName());
        scriptLog.setValue(branchOrTag);
        scriptLog = logDao.save(scriptLog);



        Project project = script.getProject();

        MDC.put("logFileId", scriptLog.getId());
        try {

            log.info("日志ID {}", scriptLog.getId());
            log.info("开始执行脚本 {} {} ", project.getName(), branchOrTag);

            GitCredential credential = gitCredentialService.findBestByUrl(project.getGitUrl());

            GitTool.CloneResult cloneResult = GitTool.clone(project.getGitUrl(), credential.getUsername(), credential.getPassword(), branchOrTag);
            File workDir = cloneResult.getDir();
            log.info("代码下载完毕 " + workDir);
            log.info("提交信息:" + cloneResult.getCodeMessage());


            Host host = script.getHost();
            Host dockerRunner = host != null ? host : hostService.getDefaultDockerRunner();
            log.info("执行主机为: {}", dockerRunner.getName());

            scriptLog.setBuildHostName(dockerRunner.getName());
            scriptLog.setCodeMessage(cloneResult.getCodeMessage());
            logDao.save(scriptLog);

            DockerClient dockerClient = dockerService.getClient(dockerRunner);


            String dockerfileContent = createDockerfile(script);
            log.info("转换为dockerfile后的脚本 \n{}", dockerfileContent);

            File target = new File(workDir, "dockerfile-script");
            FileUtils.writeStringToFile(target, dockerfileContent, StandardCharsets.UTF_8);


            log.info("向docker发送指令");
            MyBuildImageResultCallback buildCallback = new MyBuildImageResultCallback(script.getId());
            buildThreadMap.put(scriptLog.getId(), buildCallback);

            String imageId = dockerClient.buildImageCmd(workDir)

                    // 删除构建产生的容器
                    .withForcerm(true)

                    .withNetworkMode("host")
                    .withDockerfile(target)
                    .exec(buildCallback).awaitImageId();
            log.info("脚本执行结束");

            buildThreadMap.remove(scriptLog.getId());

            log.info("清理镜像");
            dockerClient.removeImageCmd(imageId).exec();
            log.info("清理结束");

            dockerClient.close();


            scriptLog.setSuccess(true);
            scriptLog.setCompleteTime(new Date());
            scriptLog.setTimeSpend(scriptLog.getCompleteTime().getTime() - scriptLog.getCreateTime().getTime());

            logDao.save(scriptLog);
        } catch (Exception e) {
            log.info("异常 {} {}", e.getClass().getName(), e.getMessage());


            if (e instanceof IllegalArgumentException && e.getMessage() != null && e.getMessage().contains("Dockerfile does not exist")) {
                log.info("请确保项目下至少有一个Dockerfile文件，不论是否指定其他Dockerfile");
            }
            e.printStackTrace();

            scriptLog.setSuccess(false);
            scriptLog.setCompleteTime(new Date());
            scriptLog.setTimeSpend(scriptLog.getCompleteTime().getTime() - scriptLog.getCreateTime().getTime());
            scriptLog.setErrMsg(e.getClass().getName() + " " + e.getMessage());
            logDao.save(scriptLog);
        }

        MDC.remove("logFileId");
    }

    private String createDockerfile(Script script) {
        StringBuilder result = new StringBuilder();
        String content = script.getContent();
        Assert.hasText(content, "脚本内容不能为空");
        result.append(content);
        String rs = result.toString();
        return rs;
    }


    private final Map<String, MyBuildImageResultCallback> buildThreadMap = new HashMap<>();


    public void stop(String id) throws IOException {
        MyBuildImageResultCallback callback = buildThreadMap.get(id);
        if (callback != null) {
            callback.close();
        }

        ScriptLog scriptLog = logDao.findById(id).get();

        scriptLog.setSuccess(false);
        scriptLog.setCompleteTime(new Date());
        scriptLog.setTimeSpend(scriptLog.getCompleteTime().getTime() - scriptLog.getCreateTime().getTime());
        logDao.save(scriptLog);
    }


}
