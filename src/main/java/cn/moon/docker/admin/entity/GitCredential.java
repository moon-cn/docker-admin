package cn.moon.docker.admin.entity;

import cn.moon.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
public class GitCredential extends BaseEntity  {


    @NotNull
    String url;


    @NotNull
    String username;

    @NotNull
    String password;


}
