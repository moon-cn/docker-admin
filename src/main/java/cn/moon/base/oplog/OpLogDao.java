package cn.moon.base.oplog;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OpLogDao extends JpaRepository<OpLog, String> {

    List<OpLog> findTop100ByCreateTimeLessThan(Date date);
}
