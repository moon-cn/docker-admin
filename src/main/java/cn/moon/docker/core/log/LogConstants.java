package cn.moon.docker.core.log;


import java.io.File;

public class LogConstants {

    public static final String URL_PREFIX = "/api/log/";


    public static String getLogViewUrl(String logger){
        String prefix = LogConstants.URL_PREFIX;

        return  prefix + logger;
    }


    public static File getLogPath(String id) {
        return new File("docker-admin-logs", id + ".log");
    }

}
