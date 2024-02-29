package cn.moon.docker.admin;

import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.admin.service.HostService;
import cn.moon.docker.core.DockerManager;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.PruneResponse;
import com.github.dockerjava.api.model.PruneType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CleanImageJob {


    @Resource
    HostService hostService;

    @Resource
    DockerManager dockerService;



    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.DAYS)
    public void run() {
        log.info("开始清理资源");

        List<Host> list = hostService.findAll();

        for (Host host : list) {

            try {
                log.info("清理服务器 {}", host.getName());

                DockerClient client = dockerService.getClient(host);


                PruneResponse response = client.pruneCmd(PruneType.IMAGES)
                        .withDangling(false) //  无名镜像
                        .withUntilFilter( (60 * 24) + "h") // 超过x天
                        .exec();

                log.info(" raw values {}", response.getRawValues());
                log.info("{} 回收空间  {}", host.getName(), response.getSpaceReclaimed());


                client.close();

            } catch (Exception e) {
                log.info("清理失败  " + e.getMessage());
                e.printStackTrace();
            }

        }





    }
}
