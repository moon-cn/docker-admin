package cn.moon.docker.admin.entity;

import cn.moon.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name","url"}))
public class ImageTag extends BaseEntity {

    String name;

    Date time;

    String url;


}
