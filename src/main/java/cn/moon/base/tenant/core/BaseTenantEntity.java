package cn.moon.base.tenant.core;

import cn.moon.base.BaseEntity;
import cn.moon.base.shiro.UserContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;


/**
 * 租户基类
 */
@Getter
@Setter
@MappedSuperclass
@FieldNameConstants
@Slf4j
@FilterDef(name = TenantConstant.FILTER_NAME,
        parameters = @ParamDef(name = "tenantId", type = "string")
)
@Filter(name = TenantConstant.FILTER_NAME, condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity extends BaseEntity {


    @Column(name = "tenant_id", length = 20)
    String tenantId;


    @Override
    public void prePersist() {
        super.prePersist();

        if(this.tenantId == null){
            this.tenantId = UserContext.current().getTenantId();
        }
    }

    @Override
    public void preUpdate() {
        super.preUpdate();

        String t = UserContext.current().getTenantId();
        if (t != null) {
            this.tenantId = t;
        }
    }
}
