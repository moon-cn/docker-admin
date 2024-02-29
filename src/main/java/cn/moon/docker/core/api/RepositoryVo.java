package cn.moon.docker.core.api;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RepositoryVo {
    String name;
    String type;
    String summary;

    String url;

    Date time;
    String latestVersion;

    String description;
    int starCount;

    boolean isOfficial;


    Long tagCount;
}
