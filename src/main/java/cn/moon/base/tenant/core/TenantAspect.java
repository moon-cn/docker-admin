package cn.moon.base.tenant.core;

import cn.moon.base.shiro.UserContext;
import cn.moon.docker.admin.dao.ProjectDao;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Aspect
@Component
public class TenantAspect {


    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    ProjectDao projectDao;

    @Before("execution(* cn.moon.base.BaseService+.find*(..))")
    public void beforeFind(JoinPoint point) throws Throwable {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter(TenantConstant.FILTER_NAME);


        String tenantId = UserContext.current().getTenantId();
        if (tenantId == null) {
            return;
        }


        Object target = point.getTarget();

        Class<?> cls = target.getClass();
        Type[] types = ((ParameterizedType) cls.getGenericSuperclass()).getActualTypeArguments();
        if(types.length != 1){
            return;
        }
        Type type = types[0];

        Class domainCls = (Class) type;

        Class baseCls = (Class) domainCls.getGenericSuperclass();
        if(baseCls == null || !baseCls.isAssignableFrom(BaseTenantEntity.class)){
            return;
        }



        Filter filter = session.enableFilter(TenantConstant.FILTER_NAME).setParameter("tenantId", tenantId);
        filter.validate();


    }
}