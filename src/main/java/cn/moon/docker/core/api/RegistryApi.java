package cn.moon.docker.core.api;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Setter
@Getter
public abstract class RegistryApi {

    protected RegistryConfig.Registry registry;


    public abstract Page<RepositoryVo> findRepositoryList(Pageable pageable, String keyword) throws Exception;


    public abstract PageImpl<TagVo> findTagList(String imageUrl, Pageable pageable) throws Exception;


}
