package cn.moon.base.tenant;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantDao extends JpaRepository<Tenant, String> {

    Tenant findByCode(String code);
}
