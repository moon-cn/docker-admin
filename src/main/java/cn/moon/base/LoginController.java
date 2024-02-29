package cn.moon.base;

import cn.moon.base.oplog.OpLogService;
import cn.moon.base.user.UserService;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录验证
 */
@RestController
@RequestMapping("api")
public class LoginController {


    @Resource
    UserService userService;

    @Resource
    OpLogService opLogService;



    @RequestMapping("login")
    public AjaxResult login(@RequestBody LoginParam loginParam) {
        String username = loginParam.username;
        String inputPassword = loginParam.password;

        UsernamePasswordToken token = new UsernamePasswordToken(username, inputPassword);

        try {
            SecurityUtils.getSubject().login(token);
            opLogService.log("登录", null,"登录成功");
            return AjaxResult.success("登录成功", getInitData());
        } catch (Exception e) {
            opLogService.log("登录",null,"登录失败");
            return AjaxResult.error("登录失败" + e.getMessage());
        }
    }



    @RequestMapping("login/check")
    public AjaxResult checkLogin() {
        boolean isLogin = SecurityUtils.getSubject().isAuthenticated();


        if (isLogin) {
            return AjaxResult.success(null, getInitData());
        }

        return AjaxResult.error("检查登录结果：未登录");
    }

    private Object getInitData() {
        Map<String,Object> data = new HashMap<>();

        Subject subject = SecurityUtils.getSubject();

        Set<String> perms = Constants.getPerms();


        Set<String> permSet = perms.stream().filter(subject::isPermitted).collect(Collectors.toSet());


        data.put("perms",permSet);


        return data;
    }

    @RequestMapping("logout")
    public AjaxResult logout() {
        SecurityUtils.getSubject().logout();
        return AjaxResult.success("退出成功");
    }


    @Getter
    @Setter
    public static class LoginParam {
        String username;
        String password;
    }
}
