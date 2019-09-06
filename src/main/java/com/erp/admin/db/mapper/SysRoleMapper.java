package com.erp.admin.db.mapper;

import java.util.List;

import com.erp.admin.db.entity.SysRole;

public interface SysRoleMapper {
    int deleteByPrimaryKey(Integer rid);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(Integer rid);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);
    
    List<SysRole> selectRoles();
    
    SysRole selectByRole(String role);
}