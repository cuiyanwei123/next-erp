package cn.exrick.xboot.modules.base.service;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.exrick.xboot.base.XbootBaseService;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.entity.User;

/**
 * 用户接口
 * 
 * @author Exrickx
 */
@CacheConfig(cacheNames = "user")
public interface UserService extends XbootBaseService<User, String> {

    /**
     * 通过用户名获取用户
     * 
     * @param username
     * @return
     */
    @Cacheable(key = "#username")
    User findByUsername(String username);

    /**
     * 多条件分页获取用户
     * 
     * @param user
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<User> findByCondition(User user, SearchVo searchVo, Pageable pageable);
}
