package cn.exrick.xboot.modules.base.dao;

import cn.exrick.xboot.base.XbootBaseDao;
import cn.exrick.xboot.modules.base.entity.User;

/**
 * 用户数据处理层
 *
 * @author Exrickx
 */
public interface UserDao extends XbootBaseDao<User, String> {

    /**
     * 通过用户名获取用户
     *
     * @param username
     * @return
     */
    User findByUsername(String username);
}
