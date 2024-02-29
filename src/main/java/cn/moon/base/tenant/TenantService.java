package cn.moon.base.tenant;

import cn.moon.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TenantService extends BaseService<Tenant> {


    @Resource
    TenantDao tenantDao;



    public Tenant findByCode(String code){
        return tenantDao.findByCode(code);
    }


}
