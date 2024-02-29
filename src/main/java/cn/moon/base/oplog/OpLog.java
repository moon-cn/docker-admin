package cn.moon.base.oplog;

import cn.moon.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
public class OpLog extends BaseEntity {


    @NotNull
    @Column(length = 50)
    String username;


    @NotNull
    String type;

    String permission;

    @Column(length = 1000)
    String msg;


    @Lob
    String request;


    @Lob
    String response;

}
