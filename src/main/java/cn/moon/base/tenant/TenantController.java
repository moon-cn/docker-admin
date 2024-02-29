package cn.moon.base.tenant;

import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import cn.moon.base.Option;
import cn.moon.docker.admin.entity.Host;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/tenant")
public class TenantController {

    @Resource
    private TenantService service;


    @RequiresPermissions("tenant:list")
    @RequestMapping("list")
    public Page<Tenant> list(@PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable, Host host) {
        Page<Tenant> list = service.findAll(pageable);

        return list;
    }

    @RequestMapping("save")
    public AjaxResult save(@RequestBody Tenant tenant) {
        tenant.setId(tenant.getCode());
        service.save(tenant);
        return AjaxResult.success("保存");
    }

    @RequestMapping("update")
    public AjaxResult update(@RequestBody Tenant u) {
        Tenant db = service.findOne(u.getId());

        service.save(u);
        return AjaxResult.success("保存");
    }


    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteById(id);
        return AjaxResult.success("删除成功");
    }


    @RequestMapping("options")
    public AjaxResult options() {
        List<Option> list = new ArrayList<>();

        List<Tenant> values = service.findAll();

        for (Tenant role : values) {
            list.add(new Option(role.getCode(), role.getName()));
        }

        return AjaxResult.success("", list);
    }


}
