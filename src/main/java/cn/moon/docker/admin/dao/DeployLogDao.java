package cn.moon.docker.admin.dao;

import cn.moon.docker.admin.entity.DeployLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeployLogDao extends JpaRepository<DeployLog,String> {
}
