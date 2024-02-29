package cn.moon.docker.admin.dao;


import cn.moon.docker.admin.entity.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface RepositoryDao extends JpaRepository<Repository,String> {
    Repository findByUrl(String url);

    Page<Repository> findByUrlLike(Pageable pageable, String keyword);
}
