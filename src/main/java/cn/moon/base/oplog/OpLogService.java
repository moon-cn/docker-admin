package cn.moon.base.oplog;

import cn.moon.base.BaseService;
import cn.moon.base.shiro.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OpLogService extends BaseService<OpLog> {


    @Transactional
    public void log(String type, String permission, String msg){
        String username = UserContext.current().getUsername();;

        if(username == null){
            return;
        }

        OpLog opLog = new OpLog();
        opLog.setMsg(msg);
        opLog.setType(type);
        opLog.setUsername(username);
        opLog.setPermission(permission);

        this.save(opLog);
    }

}
