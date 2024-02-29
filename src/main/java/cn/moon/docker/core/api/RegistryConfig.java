package cn.moon.docker.core.api;

import cn.moon.docker.core.api.impl.AliyunApi;
import cn.moon.docker.core.api.impl.DockerHubApi;
import cn.moon.docker.core.api.impl.TencentApi;
import cn.hutool.extra.spring.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "registry")
public class RegistryConfig {


    List<Registry> list;


    public Registry findByUrl(String url) {
        for (Registry registry : list) {
            if (url.startsWith(registry.getUrl())) {
                return registry;
            }
        }
        return null;
    }

    public RegistryApi findApiByUrl(String url){
        Registry registry = findByUrl(url);

        if(registry == null){
            return SpringUtil.getBean(DockerHubApi.class);
        }

        return registry.createApi();
    }

    public Registry findDefault() {
        for (Registry registry : list) {
            if(registry.defaultRegistry){
                return  registry;
            }
        }
        throw new IllegalStateException("没有设置默认注册中心");
    }


    @Getter
    @Setter
    public static class Registry {
        String username;
        String password;
        String url;
        String namespace;

        Type type;
        String apiKey;
        String apiSecret;

        boolean defaultRegistry;


        public String getFullUrl() {
            if (url != null) {
                return url + "/" + namespace;
            }

            return namespace;
        }

        public RegistryApi createApi(){
            RegistryConfig.Type type = this.getType();

            try {
                Class<? extends RegistryApi> cls = type.getApiClass();
                RegistryApi registryApi = cls.getConstructor().newInstance();
                registryApi.setRegistry(this);
                return registryApi;
            }catch (Exception e){
                throw new  IllegalStateException(e);
            }
        }

        @Override
        public String toString() {
            return getFullUrl();
        }
    }


    @Getter
    @AllArgsConstructor
    public enum Type {
        ALIYUN("阿里云镜像仓库", AliyunApi.class),
        TENCENT("腾讯云镜像仓库", TencentApi.class);


        String label;
        Class<? extends RegistryApi> apiClass;

    }
}
