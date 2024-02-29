package cn.moon.base.tool;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.*;

public class YamlTool {

    /**
     * yml文件流转成单层map
     * 转Properties 改变了顺序
     *
     * @param yamlContent
     * @return
     */
    public static Map<String, Object> yamlToFlattenedMap(String yamlContent) {
        if(StrUtil.isBlank(yamlContent)){
            return Collections.emptyMap();
        }
        Yaml yaml = createYaml();
        Map<String, Object> map=new HashMap<>();
        Iterable<Object> ite = yaml.loadAll(yamlContent);
        for (Object object : ite) {
            if (object != null) {
                map = asMap(object);
                map=getFlattenedMap(map);
            }
        }
        return map;
    }

    private static Yaml createYaml() {
        return new Yaml(new Constructor());
    }

    private static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }
    private static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isBlank(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            } else {
                result.put(key, (value != null ? value.toString() : ""));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object object) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }



    public static void main(String[] args) {
        String str = "db_ip: 100.116.29.47\n" +
                   "db_database: wuye_llstc\n" +
                   "server:\n" +
                   "  port: 80\n" +
                   "spring:\n" +
                   "  datasource:\n" +
                   "    username: wuye_llstc\n" +
                   "    password: wuYe2022!\n" +
                   "ykt:\n" +
                   "  api:\n" +
                   "    appKey: 1f02bf65a7ac4aa5a5f3ae320192f6fb\n" +
                   "    appSecret: 81d67e86174d497683aa266b5a834b73\n" +
                   "    baseUrl: http://10.139.64.24:8081/api/gateway\n" +
                   "  hostAddr: https://zy.crecct.cn\n" +
                   "\n" +
                   "\n" +
                   "  mpabout:\n" +
                   "    name: 中铁彩虹一卡通\n" +
                   "    version: 1.1.0\n" +
                   "    servicePhone: 0854-5671102\n" +
                   "    copyright: 中铁文旅创新产业研究院提供技术支持\n" +
                   "\n" +
                   "jiashicang:\n" +
                   "  url: jdbc:mysql://100.116.29.47:3306/jiashicang?autoReconnect=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&nullCatalogMeansCurrent=true\n" +
                   "  username: jiashicang\n" +
                   "  password: Crec2022!!";



        Map<String, Object> dict2 = yamlToFlattenedMap(str);


    }
}
