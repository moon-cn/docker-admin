package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.admin.service.HostService;
import cn.moon.base.Option;
import cn.moon.docker.admin.bean.DockerInfo;
import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api/host")
public class HostController  {


    @Resource
    private HostService service;

    @RequiresPermissions("host:list")
    @RequestMapping("list")
    public Page<Host> list(@PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable, Host host) {
        Page<Host> list = service.findAll(pageable);
        return list;
    }

    @RequestMapping("save")
    public AjaxResult update(@RequestBody Host host) {
        service.save(host);
        return AjaxResult.success("保存");
    }


    @RequestMapping("delete")
    public AjaxResult delete(@RequestBody Host host) {
        service.deleteById(host.getId());
        return AjaxResult.success("删除成功");
    }


    @RequestMapping("options")
    public List<Option> options() {
        List<Host> list = service.findAll();
        List<Option> options = new ArrayList<>();
        for(Host h: list){
            options.add(new Option(h.getId(), h.getName()));
        }
        return   options;
    }

    @RequestMapping("get")
    public Map<String, Object> get(String id) {
        Host host = service.findOne(id);
        Info info = service.getDockerInfo(host);

        DockerInfo dockerInfo = new DockerInfo();
        BeanUtils.copyProperties(info, dockerInfo);



        Map<String, Object> result = new HashMap<>();
        result.put("host", host);
        result.put("info", dockerInfo);

        return result;
    }


    @RequestMapping("containers")
    public List<Container> containers(String id) {
        return service.getContainers(id);
    }

    @RequestMapping("images")
    public List<Image> images(String id) {
        return service.getImages(id);
    }

    @RequestMapping("deleteImage")
    public AjaxResult deleteImage(String id, String imageId) {
        try {
            service.deleteImage(id, imageId);
        } catch (ConflictException e) {
            return AjaxResult.error("删除镜像失败" + e.getMessage());
        }
        return AjaxResult.success();
    }

}
