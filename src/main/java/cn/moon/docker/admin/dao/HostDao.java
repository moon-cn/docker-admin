package cn.moon.docker.admin.dao;


import cn.moon.docker.admin.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostDao extends JpaRepository<Host, String> {

    Host findTop1ByIsRunnerOrderByModifyTimeDesc(boolean isRunner);


}
