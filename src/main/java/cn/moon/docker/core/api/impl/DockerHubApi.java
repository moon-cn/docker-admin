package cn.moon.docker.core.api.impl;

import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.admin.service.HostService;
import cn.moon.base.tool.JsonTool;
import cn.moon.docker.core.DockerManager;
import cn.moon.docker.core.api.RepositoryVo;
import cn.moon.docker.core.api.RegistryApi;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.SearchItem;
import com.github.kevinsawicki.http.HttpRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// registry.cn-hangzhou.aliyuncs.com
@Slf4j
@Component // 默认只有一个
public class DockerHubApi extends RegistryApi {

    @Resource
    HostService hostService;

    @Resource
    DockerManager dockerManager;

    public Page<RepositoryVo> findRepositoryList(Pageable pageable, String keyword) throws Exception {
        if (keyword == null) {
            return Page.empty();
        }
        Host host = hostService.getDefaultDockerRunner();


        DockerClient client = dockerManager.getClient(host);

        List<SearchItem> list = client.searchImagesCmd(keyword).exec();

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<RepositoryVo> resultList = list.stream().map(i -> {
            RepositoryVo r = new RepositoryVo();
            r.setName(i.getName());
            r.setDescription(i.getDescription());
            r.setStarCount(i.getStarCount());
            r.setOfficial(i.isOfficial());
            return r;
        }).collect(Collectors.toList());

        return new PageImpl<>(resultList);
    }


    // https://hub.docker.com/v2/namespaces/nacos/repositories/nacos-server/tags
    //https://hub.docker.com/v2/repositories/nacos/nacos-server/tags
    // https://hub.docker.com/v2/repositories/library/redis/tags
    @Override
    public PageImpl<cn.moon.docker.core.api.TagVo> findTagList(String url, Pageable pageable) throws ClientException, IOException {

        if (!url.contains("/")) {
            // 官方镜像
            url = "library/" + url;
        }
        String api = "https://registry.hub.docker.com/v2/repositories/" + url + "/tags";
        log.info("请求dockerhub api {}", api);
        String body = HttpRequest.get(api).body();


        TagPageResult result = JsonTool.jsonToBean(body, TagPageResult.class);
        System.out.println(JsonTool.toPrettyJsonQuietly(result));

        List<TagVo> voList = result.getResults();

        List<cn.moon.docker.core.api.TagVo> collect = voList.stream().map(v -> new cn.moon.docker.core.api.TagVo()).collect(Collectors.toList());

        return new PageImpl<>(collect, pageable, result.getCount());
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class TagPageResult {
        int count;
        String next;
        String previous;
        List<TagVo> results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class TagVo {
        String name;
    }
}
