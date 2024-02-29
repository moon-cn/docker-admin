package cn.moon.docker.admin.entity;

import cn.moon.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
public class Script extends BaseEntity {

    @NotNull
    @ManyToOne
    Project project;

    @NotNull
    @Column(unique = true)
    String name;



    @NotNull
    @Lob
    String content;



    @ManyToOne
    Host host;


    String cron;


}
