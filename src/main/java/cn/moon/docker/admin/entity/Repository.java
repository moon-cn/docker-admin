package cn.moon.docker.admin.entity;

import cn.moon.base.tenant.core.BaseTenantEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class Repository extends BaseTenantEntity {

    String name;
    String type;
    String summary;

    @Column(unique = true)
    String url;

    Date time;
    String latestVersion;

    String description;
    int starCount;

    boolean isOfficial;


    Long tagCount;





}
