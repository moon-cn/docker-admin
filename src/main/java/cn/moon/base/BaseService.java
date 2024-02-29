package cn.moon.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class BaseService<T> {

    @Autowired
    protected JpaRepository<T,String> baseDao;


    public T findOne(String id) {
       return baseDao.findById(id).orElse(null);
    }

    public T save(T t) {
     return   baseDao.save(t);
    }


    public List<T> findAll(){
        return baseDao.findAll();
    }

    public Page<T> findAll(Pageable pageable) {
     return    baseDao.findAll(pageable);
    }


    public List<T> findAll(Sort sort) {
        return    baseDao.findAll(sort);
    }

    public Page<T> findAll(T exampleBean, Pageable pageable) {
        return baseDao.findAll(Example.of(exampleBean), pageable);
    }


    public Page<T> findAll(Example<T> example, Pageable pageable) {
        return baseDao.findAll(example, pageable);
    }

    public void deleteById(String id) {
        baseDao.deleteById(id);
    }
}
