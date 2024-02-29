package cn.moon.base.user;

import cn.moon.base.AjaxResult;
import cn.moon.base.BaseEntity;
import cn.moon.base.Option;
import cn.moon.base.role.Role;
import cn.moon.base.tenant.Tenant;
import cn.moon.base.tenant.TenantService;
import cn.moon.docker.admin.entity.Host;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
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
@RequestMapping("api/user")
@RequiresRoles("admin")
public class UserController {


    @Resource
    private UserService service;

    @Resource
    private TenantService tenantService;


    @RequiresPermissions("user:list")
    @RequestMapping("list")
    public Page<User> list(@PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable, Host host) {
        Page<User> list = service.findAll(pageable);

        return list;
    }


    @RequestMapping("save")
    public AjaxResult save(@RequestBody User user) {
        Tenant tenant = user.getTenant();
        if(tenant != null && tenant.getCode() != null){
            tenant = tenantService.findByCode(tenant.getCode());
        }else {
            tenant = null;
        }
        user.setTenant(tenant);
        service.save(user);
        return AjaxResult.success("保存");
    }

    @RequestMapping("update")
    public AjaxResult update(@RequestBody User input) {
        User db = service.findOne(input.getId());
        if(StrUtil.isEmpty(input.getPassword())){
            input.setPassword(db.getPassword());
        }
        Tenant tenant = input.getTenant();
        if(tenant != null && tenant.getCode() != null){
            tenant = tenantService.findByCode(tenant.getCode());
        }else {
            tenant = null;
        }
        input.setTenant(tenant);
        service.save(input);
        return AjaxResult.success("保存");
    }


    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteById(id);
        return AjaxResult.success("删除成功");
    }



    @RequestMapping("roleOptions")
    public AjaxResult roleOptions() {
        List<Option> list = new ArrayList<>();

        Role[] values = Role.values();

        for (Role role : values) {
            list.add(new Option(role.name(), role.getLabel()));
        }

        return AjaxResult.success("", list);
    }


}
