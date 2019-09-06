package com.erp.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author
 * @version : 1.00
 * @Copyright http://www.onehome.cn/
 * @Description : SpringApplication项目入口
 * @History：Editor version Time Operation Description*
 *
 */
@SpringBootApplication
@ComponentScan
@MapperScan("com.erp.admin.db.mapper")
@EnableTransactionManagement
@EnableSwagger2
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
