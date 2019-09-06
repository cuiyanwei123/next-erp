package com.erp.admin.db.mapper;

import java.util.List;

import com.erp.admin.db.entity.SysUser;

public interface SysUserMapper {

    int deleteByPrimaryKey(Integer uid);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(Integer uid);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);

    List<SysUser> getUsersByRole(String role);

    SysUser findByUsername(String username);

    SysUser findByMobile(String mobile);

    Integer ifNewest(Integer id);
}