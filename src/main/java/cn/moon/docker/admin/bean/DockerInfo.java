package cn.moon.docker.admin.bean;

import com.github.dockerjava.api.model.InfoRegistryConfig;
import com.github.dockerjava.api.model.SwarmInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DockerInfo implements Serializable {

    private String architecture;

    private Integer containers;

    private Integer containersStopped;

    private Integer containersPaused;

    private Integer containersRunning;

    private Boolean cpuCfsPeriod;

    private Boolean cpuCfsQuota;

    private Boolean cpuShares;

    private Boolean cpuSet;

    private Boolean debug;

    private String discoveryBackend;

    private String dockerRootDir;

    private String driver;

    private List<List<String>> driverStatuses;

    private List<Object> systemStatus;

    private Map<String, List<String>> plugins;

    private String executionDriver;

    private String loggingDriver;

    private Boolean experimentalBuild;

    private String httpProxy;

    private String httpsProxy;

    private String id;

    private Boolean ipv4Forwarding;

    private Boolean bridgeNfIptables;

    private Boolean bridgeNfIp6tables;

    private Integer images;

    private String indexServerAddress;

    private String initPath;

    private String initSha1;

    private String kernelVersion;

    private String[] labels;

    private Boolean memoryLimit;

    private Long memTotal;

    private String name;

    private Integer ncpu;

    private Integer nEventsListener;

    private Integer nfd;

    private Integer nGoroutines;

    private String noProxy;

    private Boolean oomKillDisable;

    private String osType;

    private Integer oomScoreAdj;

    private String operatingSystem;

    private InfoRegistryConfig registryConfig;

    private String[] sockets;

    private Boolean swapLimit;

    private String systemTime;

    private String serverVersion;

    private String clusterStore;

    private String clusterAdvertise;

    private SwarmInfo swarm;

    private String isolation;

    private List<String> securityOptions;
}
