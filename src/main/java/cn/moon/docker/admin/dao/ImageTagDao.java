package cn.moon.docker.admin.dao;

import cn.moon.docker.admin.entity.ImageTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageTagDao extends JpaRepository<ImageTag, String> {

    public long countByUrlAndName(String url, String name);

    List<ImageTag> findByUrlOrderByTime(String url);
}
