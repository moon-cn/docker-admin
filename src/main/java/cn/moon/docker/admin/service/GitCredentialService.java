package cn.moon.docker.admin.service;

import cn.moon.docker.admin.entity.GitCredential;
import cn.moon.docker.admin.dao.GitCredentialDao;
import cn.moon.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GitCredentialService extends BaseService<GitCredential> {


    @Resource
    GitCredentialDao dao;


    public GitCredential findBestByUrl(String gitUrl) {
        List<GitCredential> list = dao.findAll();


        // url排序， 从长到短
        Collections.sort(list, (a, b) -> b.getUrl().length() - a.getUrl().length());


        for (GitCredential credential : list) {
            if (gitUrl.startsWith(credential.getUrl())) {
                return credential;
            }

        }

        return null;


    }


}
