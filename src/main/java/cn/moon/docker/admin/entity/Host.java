package cn.moon.docker.admin.entity;

import cn.moon.base.tenant.core.BaseTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * 主机信息
 */
@Entity
@Getter
@Setter
@ToString
@FieldNameConstants
public class Host extends BaseTenantEntity {

    @NotNull
    @Column(unique = true)
    String name;
    Boolean isRunner;


    String dockerHost;
    String dockerHostHeader;

    String remark;

    @Override
    public void prePersist() {
        super.prePersist();
        if(isRunner == null){
            isRunner = false;
        }
    }
}
