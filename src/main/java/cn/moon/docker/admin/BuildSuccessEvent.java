package cn.moon.docker.admin;

import cn.moon.docker.admin.entity.BuildLog;
import cn.moon.base.BaseEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BuildSuccessEvent extends BaseEvent {


    BuildLog buildLog;
    String version;

    public BuildSuccessEvent(Object source) {
        super(source);
    }
}
