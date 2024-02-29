package cn.moon.docker.admin.entity;

import cn.moon.base.tenant.core.BaseTenantEntity;
import cn.moon.docker.admin.entity.converter.AppConfigConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class App extends BaseTenantEntity {

    @NotNull
    @Column(unique = true)
    String name;


    @ManyToOne
    Host host;

    @Transient
    String hostId;

    String imageUrl;
    String imageTag;


    Boolean autoDeploy;

    Boolean autoRestart;

    @Transient
    String logUrl;



    @Lob
    @Convert(converter = AppConfigConverter.class)
    AppConfig config;




    @Override
    public void prePersist() {
        super.prePersist();
        if(autoDeploy == null){
            autoDeploy = true;
        }

    }


    @Data
    public static class AppConfig {


        String image;
        boolean privileged;

        boolean restart; // always, no

        String cmd; //启动命令

        String extraHosts; // ip映射

        // 主机:容器
        List<PortBinding> ports = new ArrayList<>(); //  - 7100:7100/udp  - 7100:7100/tcp

        /**
         * /var/run/docker.sock:/var/run/docker.sock:ro
         * /var/run/docker.sock:/var/run/docker.sock:rw
         */
        List<BindConfig> binds = new ArrayList<>();


        String environmentYAML;

        String networkMode;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class BindConfig {
        String publicVolume;
        String privateVolume;
        Boolean readOnly; // ro, rw

    }




    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class PortBinding {
        Integer publicPort;
        Integer privatePort;
        String protocol;

    }



}
