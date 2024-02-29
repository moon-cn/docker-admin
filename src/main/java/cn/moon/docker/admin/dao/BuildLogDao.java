package cn.moon.docker.admin.dao;

import cn.moon.docker.admin.entity.BuildLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BuildLogDao extends JpaRepository<BuildLog,String> {
}
