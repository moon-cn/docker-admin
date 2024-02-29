package cn.moon.docker.admin.bean;

import com.github.dockerjava.api.model.Container;
import lombok.Data;

import java.io.Serializable;

@Data
public class ContainerVo implements Serializable {
    private static final long serialVersionUID = 1L;

    public ContainerVo(Container c) {
        if (c == null) {
            return;
        }
        image = c.getImage();
        name = c.getNames()[0].substring(1);
        state = c.getState();
        id = c.getId();

        status = c.getStatus();
        resetStatus();
    }

    private void resetStatus() {
        status = status.replace("Up", "运行")
                .replace("Exited", "退出")

                .replace("a minute", "一分钟")
                .replace("About", "约")
                .replace("ago", "前")
                .replace("seconds", "秒")
                .replace("second", "秒")

                .replace("minutes", "分钟");
    }

    String id;
    String image;
    String name;

    String state;
    String status;



}
