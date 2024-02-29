package cn.moon.base.tenant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Entity
@Getter
@Setter
@ToString
@FieldNameConstants
public class Tenant  {

    @Id
    String id;

    @NotNull
    @Column(unique = true)
    String name;

    @NotNull
    @Column(updatable = false, unique = true)
    String code;

    @Column(updatable = false)
     Date createTime;

     Date modifyTime;


    @PrePersist
    public void prePersist() {
        this.modifyTime = this.createTime = new Date();

    }

    @PreUpdate
    public void preUpdate() {
        this.modifyTime = new Date();
    }
}
