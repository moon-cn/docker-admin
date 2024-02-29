package cn.moon.base.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 由于项目简单，这里简单使用枚举存储角色及角色对应的权限
 */
@AllArgsConstructor
@Getter
public enum Role {

    admin("管理员",new String[]{"*"}),

    simple("普通用户",new String[]{"project:*", "app:*"}),
    deploy("打包用户",new String[]{"project:list","project:build","app:list", "app:deploy"});


    private final String label;
    private final String[] perms;

}
