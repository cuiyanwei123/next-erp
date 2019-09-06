package com.erp.admin.db.mapper;

import com.erp.admin.db.entity.SysRoleUser;
import com.erp.admin.db.entity.SysUser;

public interface SysRoleUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysRoleUser record);

    int insertSelective(SysRoleUser record);

    SysRoleUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysRoleUser record);

    int updateByPrimaryKey(SysRoleUser record);
    
    SysUser findByUsername(String username);
}