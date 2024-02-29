
package cn.moon.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public AjaxResult ex(Exception e) {
        log.error("异常", e);
        return AjaxResult.error(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public AjaxResult ex(UnauthorizedException e) {
        log.error("异常", e);
        AjaxResult rs = AjaxResult.error(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        rs.setCode(403);
        return rs;
    }

}


