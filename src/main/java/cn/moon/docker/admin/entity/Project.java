package cn.moon.docker.admin.entity;

import cn.moon.base.tenant.core.BaseTenantEntity;
import cn.hutool.core.lang.Validator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@FieldNameConstants
public class Project extends BaseTenantEntity {

    @NotNull
    @Column(unique = true)
    String name;


    //默认的dockerfile
    String dockerfile;
    String defaultVersion;

    // 默认分支
    String branch;

    String gitUrl;


    private void checkName() {
        Assert.state(Validator.isGeneral(name.replaceAll("-","")), "名称为英文字母 、数字、横线、下划线");
        Assert.state(name.toLowerCase().equals(name), "名称不能包含大写");
    }

    @Override
    public void prePersist() {
        super.prePersist();
        checkName();
    }

    @Override
    public void preUpdate() {
        super.preUpdate();

        checkName();
    }
}
