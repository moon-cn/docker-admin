package cn.moon.docker.core.api.impl;

import cn.moon.docker.core.api.RegistryApi;
import cn.moon.docker.core.api.RepositoryVo;
import cn.moon.docker.core.api.TagVo;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.tcr.v20190924.TcrClient;
import com.tencentcloudapi.tcr.v20190924.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TencentApi extends RegistryApi {

    @Override
    public Page<RepositoryVo> findRepositoryList(Pageable pageable, String keyword) throws Exception {
        Credential cred = new Credential(registry.getApiKey(), registry.getApiSecret());

        TcrClient client = new TcrClient(cred, "ap-guangzhou");

        DescribeRepositoryOwnerPersonalRequest req = new DescribeRepositoryOwnerPersonalRequest();
        DescribeRepositoryOwnerPersonalResponse resp = client.DescribeRepositoryOwnerPersonal(req);

        RepoInfoResp data = resp.getData();

        List<RepositoryVo> voList = new ArrayList<>();
        for (RepoInfo info : data.getRepoInfo()) {
            RepositoryVo vo = new RepositoryVo();
            vo.setName( StrUtil.subAfter(info.getRepoName(), "/", true) );
            vo.setTime(DateUtil.parseDateTime(info.getUpdateTime()));
            vo.setDescription(info.getDescription());
            vo.setType(info.getRepoType());
            vo.setTagCount(info.getTagCount());
            vo.setUrl(registry.getUrl() + "/" + vo.getName());


            voList.add(vo);
        }

        PageImpl<RepositoryVo> page = new PageImpl<>(voList, pageable, data.getTotalCount());

        return page;
    }

    @Override
    public PageImpl<TagVo> findTagList(String imageUrl, Pageable pageable) throws Exception {
        String repoName = imageUrl.replace(registry.getUrl(), "");
        repoName = StrUtil.removePrefix(repoName,"/");

        Credential cred = new Credential(registry.getApiKey(), registry.getApiSecret());
        TcrClient client = new TcrClient(cred, "ap-guangzhou");

        DescribeImagePersonalRequest req = new DescribeImagePersonalRequest();
        req.setRepoName(repoName);

        DescribeImagePersonalResponse resp = client.DescribeImagePersonal(req);

        TagInfoResp data = resp.getData();

        List<TagVo> voList = new ArrayList<>();
        for (TagInfo info : data.getTagInfo()) {
            TagVo vo = new TagVo();
            vo.setName(info.getTagName());
            vo.setTime(DateUtil.parseDateTime(info.getUpdateTime()));

            voList.add(vo);
        }

        PageImpl<TagVo> page = new PageImpl<>(voList, pageable, data.getTagCount());

        return page;
    }


}
