package cn.moon.base.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class UserContext {

    public static CurrentUser current(){
        try {
            Subject subject = SecurityUtils.getSubject();
            if(subject.isAuthenticated()){
                Object principal = subject.getPrincipal();
                CurrentUser info = (CurrentUser) principal;
                return info;
            }
        }catch (Exception e){

        }
        return  new CurrentUser();
    }
}
