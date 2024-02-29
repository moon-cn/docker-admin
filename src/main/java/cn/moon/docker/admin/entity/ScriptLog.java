package cn.moon.docker.admin.entity;

import cn.moon.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Date;

@Entity
@Getter
@Setter
@FieldNameConstants
public class ScriptLog extends BaseEntity {

    String scriptId;
    String scriptName;

    String codeMessage;

    String buildHostName;

    String projectName;
    String projectId;

    String imageUrl;

    Date completeTime;

    Boolean success;

    String value;

    String version;


    String logUrl;



    Long timeSpend; // 用时， 毫秒

    String errMsg;

    @Lob
    String content;
}
