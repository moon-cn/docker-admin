package cn.moon.base.oplog;

import cn.moon.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("api/oplog")
public class OpLogController {


    @Resource
    private OpLogService service;


    @RequiresPermissions("oplog:list")
    @RequestMapping("list")
    public Page<OpLog> list(@PageableDefault(sort = BaseEntity.Fields.modifyTime, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OpLog> list = service.findAll(pageable);

        return list;
    }


}
