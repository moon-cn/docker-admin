package cn.moon.base.oplog;

import cn.moon.base.shiro.UserContext;
import cn.hutool.core.util.StrUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Aspect
@Component
public class OpLogAOP {

    @Resource
    OpLogService opLogService;


    @AfterReturning(value = "@annotation(org.apache.shiro.authz.annotation.RequiresPermissions)", returning = "result")
    public void afterCompletion(JoinPoint joinPoint, Object result) throws Exception {
        String username = UserContext.current().getUsername();;
        if (username == null) {
            return;
        }

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;


        RequiresPermissions ann = methodSignature.getMethod().getAnnotation(RequiresPermissions.class);
        if (ann == null) {
            return;
        }

        String[] value = ann.value();
        String permission = StrUtil.join(",", value);


        Object[] args = joinPoint.getArgs();



        StringBuilder sb = new StringBuilder();
        String[] parameterNames = methodSignature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null || arg instanceof HttpServletRequest || arg instanceof HttpSession) {
                continue;
            }


            if (arg instanceof Pageable) {
                arg = "分页参数";
            }

            String name = parameterNames[i];
            sb.append(name).append("=").append(arg).append("\n");

        }

        if (result instanceof Page) {
            result = "分页数据";
        }


        String type = joinPoint.getTarget().getClass().getSimpleName() +"." + methodSignature.getName();

        OpLog opLog = new OpLog();
        opLog.setMsg(null);
        opLog.setType(type);
        opLog.setUsername(username);
        opLog.setPermission(permission);
        opLog.setRequest(sb.toString());
        opLog.setResponse(result.toString());

        opLogService.save(opLog);
    }

    @AfterThrowing("@annotation(org.apache.shiro.authz.annotation.RequiresPermissions)")
    public void refund() {
        System.out.println("refund refund refund");
    }


}
