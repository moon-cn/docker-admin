package cn.moon.docker.core.api.impl;

import cn.moon.docker.core.api.RepositoryVo;
import cn.moon.docker.core.api.TagVo;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.moon.docker.core.api.RegistryApi;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
public class AliyunApi extends RegistryApi {


    public Page<RepositoryVo> findRepositoryList(Pageable pageable, String keyword) throws Exception {
        IAcsClient client = getClient();
        CommonRequest request = getCommonRequest();


        request.setUriPattern("/repos/" + registry.getNamespace());
        String requestBody = "" +
                             "{}";
        request.setHttpContent(requestBody.getBytes(), "utf-8", FormatType.JSON);
        request.putQueryParameter("PageSize", String.valueOf(pageable.getPageSize()));
        request.putQueryParameter("Page", String.valueOf(pageable.getPageNumber() + 1));
        if (keyword != null) {
            request.putQueryParameter("RepoNamePrefix", keyword);
        }
        CommonResponse response = client.getCommonResponse(request);
        String data = response.getData();


        return convertRepository(data, pageable);
    }


    @Override
    public PageImpl<TagVo> findTagList(String imageUrl, Pageable pageable) throws ClientException {
        int page = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        CommonRequest request = getCommonRequest();

        String subUrl = imageUrl.substring(imageUrl.indexOf("/") + 1);


        request.setUriPattern("/repos/" + subUrl + "/tags");
        String requestBody = "{}";
        request.setHttpContent(requestBody.getBytes(), "utf-8", FormatType.JSON);

        request.putQueryParameter("PageSize", String.valueOf(pageSize)); // pageSize 必须是大于0的整数并且小于等于100的整数

        request.putQueryParameter("Page", String.valueOf(page)); // 新版本是PageNo, 这里还用Page

        IAcsClient client = getClient();

        CommonResponse response = client.getCommonResponse(request);
        String data = response.getData();


        JSON json = JSONUtil.parse(data);

        JSON jsonData = json.getByPath("data", JSON.class);


        JSONArray tags = jsonData.getByPath("tags", JSONArray.class);

        List<JSONObject> tagList = tags.toList(JSONObject.class);

        Integer total = (Integer) jsonData.getByPath("total");

        List<TagVo> imageTagList = tagList.stream().map(tag -> {
            TagVo t = new TagVo();
            t.setTime(new Date(tag.getLong("imageUpdate")));
            t.setName(tag.getStr("tag"));
            return t;
        }).collect(Collectors.toList());


        return new PageImpl<>(imageTagList, pageable, total);
    }


    private CommonRequest getCommonRequest() {
        String url = registry.getUrl();
        String[] split = url.split("\\.");
        String regionId = split[1];

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.GET);
        request.setDomain("cr." + regionId + ".aliyuncs.com");
        request.setVersion("2016-06-07");
        request.putHeadParameter("Content-Type", "application/json");

        return request;
    }

    private IAcsClient getClient() {
        String host = this.registry.getUrl();
        String[] split = host.split("\\.");
        String regionId = split[1];

        DefaultProfile profile = DefaultProfile.getProfile(
                regionId,
                "LTAI4GF9k6GS86QtmLJQ5BGN",
                "zk12LKKoMDQgM12vGjVq4cadAjQXdn");

        IAcsClient client = new DefaultAcsClient(profile);

        return client;
    }

    private Page<RepositoryVo> convertRepository(String json, Pageable pageable) throws IOException {

        JSON response = JSONUtil.parse(json);


        JSON data = (JSON) response.getByPath("data");

        Integer total = (Integer) data.getByPath("total");

        List<RepositoryVo> list = new ArrayList<>();
        List<Map<String,Object>> repos = (List<Map<String,Object>>) data.getByPath("repos");
        repos.forEach(aliRepos -> {
            RepositoryVo r = new RepositoryVo();
            r.setName((String) aliRepos.get("repoName"));
            r.setSummary((String) aliRepos.get("summary"));
            r.setType((String) aliRepos.get("repoType"));
            r.setTime(new Date((Long) aliRepos.get("gmtModified")));

            Map<String, String> repoDomainList = (Map<String, String>) aliRepos.get("repoDomainList");

            String domain = repoDomainList.get("public");
            Object namespace = aliRepos.get("repoNamespace");
            String url = domain + "/" + namespace + "/" + r.getName();

            r.setUrl(url);


            list.add(r);
        });


        Page<RepositoryVo> page = new PageImpl<>(list, pageable, total);

        return page;
    }
}
