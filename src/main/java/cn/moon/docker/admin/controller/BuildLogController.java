package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.entity.BuildLog;
import cn.moon.docker.admin.service.BuildLogService;
import cn.moon.docker.core.log.LogConstants;
import cn.hutool.core.date.DateUtil;
import cn.moon.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping(value = "api/buildLog")
public class BuildLogController {

    @Resource
    private BuildLogService service;

    @RequestMapping("list")
    public Page<BuildLog> list(String projectId, @PageableDefault(sort = BaseEntity.Fields.createTime, direction = Sort.Direction.DESC) Pageable pageable) {
        BuildLog ex = new BuildLog();
        ex.setProjectId(projectId);
        Page<BuildLog> page = service.findAll(ex, pageable);


        for (BuildLog log : page) {
            log.setLogUrl(LogConstants.getLogViewUrl(log.getId()));
            if (log.getTimeSpend() == null) {
                log.setTimeSpend(DateUtil.date().getTime() - log.getCreateTime().getTime());
            }
        }

        return page;
    }


}
