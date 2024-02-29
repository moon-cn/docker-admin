package cn.moon.docker.admin.dao;


import cn.moon.docker.admin.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptDao extends JpaRepository<Script, String> {

    Script findByName(String name);

}
