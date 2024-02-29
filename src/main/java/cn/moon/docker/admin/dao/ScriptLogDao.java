package cn.moon.docker.admin.dao;

import cn.moon.docker.admin.entity.ScriptLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptLogDao extends JpaRepository<ScriptLog,String> {
}
