package com.erp.admin.db;


import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSession;
/**
 * 
 * @version : 1.00
 * @Copyright Beijing Honythink Tech Co.,Ltd
 * @Create Time : 2014-7-14 下午1:00:27
 * @Description :  供mybatis获取连接类
 * @History：Editor　　　version　　　Time　　　　　Operation　　　　　　　Description*
 *
 */
public class Connection {

	@Resource(name = "sqlSession")
	private SqlSession sqlSession;

	/**
	 * @Description ： 从连接池获取一个会话连接
	 */
	protected SqlSession getSqlSession() {
		return sqlSession;
	}

	/**
	 * @param : id 需要从map中调用的sql语句的对应id
	 * @Description ： 自动组装调用sql的句式
	 */
	protected String call(String id){
		return this.getClass().getInterfaces()[0].getName().concat(".").concat(id);
	}
}