package cn.moon.docker.admin.dao;


import cn.moon.docker.admin.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDao extends JpaRepository<App,String> {
}
