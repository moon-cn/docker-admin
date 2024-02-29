package cn.moon.base.oplog;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CleanLogJob {


    @Resource
    OpLogDao opLogDao;


    @Scheduled(fixedRate = 5, initialDelay = 1, timeUnit = TimeUnit.DAYS)
    public void run() {
        log.info("开始清理日志");
        Date now = new Date();
        Date lastMonth = DateUtils.addMonths(now, -1);

        long count = opLogDao.count();
        while (count > 10000) {
            List<OpLog> list = opLogDao.findTop100ByCreateTimeLessThan(lastMonth);
            opLogDao.deleteAll(list);

            count = opLogDao.count();
        }

    }
}
