package cn.moon.base;

import cn.hutool.extra.spring.SpringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

public class Constants {



    public static Set<String> getPerms() {
        Set<String> perms = new LinkedHashSet<>();
        Map<String, RequestMappingHandlerMapping> mappingMap = SpringUtil.getApplicationContext().getBeansOfType(RequestMappingHandlerMapping.class);

        Collection<RequestMappingHandlerMapping> mappings = mappingMap.values();
        for (RequestMappingHandlerMapping mapping : mappings) {
            Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

            for (Map.Entry<RequestMappingInfo, HandlerMethod> e : map.entrySet()) {
                HandlerMethod handlerMethod = e.getValue();

                if (!handlerMethod.hasMethodAnnotation(RequiresPermissions.class)) {
                    continue;
                }

                RequiresPermissions isPermittedission = handlerMethod.getMethodAnnotation(RequiresPermissions.class);


                Collections.addAll(perms, isPermittedission.value());

            }
        }

        return perms;
    }
}
