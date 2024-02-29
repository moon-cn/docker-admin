package cn.moon.base.user;

import cn.moon.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserService extends BaseService<User> {


    @Resource
    UserDao userDao;




    public long count() {

        return userDao.count();
    }
}
