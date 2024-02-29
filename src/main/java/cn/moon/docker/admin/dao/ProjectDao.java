package cn.moon.docker.admin.dao;

import cn.moon.docker.admin.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectDao extends JpaRepository<Project,String> {


    Project findByName(String name);
}
