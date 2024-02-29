package cn.moon.docker.admin.service;

import cn.moon.docker.admin.dao.HostDao;
import cn.moon.docker.admin.entity.Host;
import cn.moon.base.BaseService;
import cn.moon.docker.core.DockerManager;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class HostService extends BaseService<Host> {


    @Resource
    DockerManager dockerService;

    @Resource
    HostDao hostDao;

    /**
     * 获得镜像构建主机
     * @return
     */
    public Host getDefaultDockerRunner(){
        Host host = hostDao.findTop1ByIsRunnerOrderByModifyTimeDesc(true);

        return host;
    }


    public Info getDockerInfo(Host host) {
        DockerClient client = dockerService.getClient(host);
        Info info = client.infoCmd().exec();

        return info;
    }


    public List<Container> getContainers(String id) {
        Host db = this.findOne(id);
        DockerClient client = dockerService.getClient(db);

        List<Container> list = client.listContainersCmd().withShowAll(true).exec();
        return list;
    }

    public List<Image> getImages(String id) {
        Host db = this.findOne(id);
        DockerClient client = dockerService.getClient(db);

        List<Image> list = client.listImagesCmd().withShowAll(true).exec();
        return list;
    }

    public void deleteImage(String hostId, String imageId) {
        Host db = this.findOne(hostId);
        DockerClient client = dockerService.getClient(db);

        client.removeImageCmd(imageId).exec();

    }


    public long count() {

        return hostDao.count();
    }
}
