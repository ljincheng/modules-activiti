package cn.booktable.appadmin.config;

import cn.booktable.core.page.PageInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MyBatis配置
 * @author ljc
 */
@Configuration
public class MyBatisConfig {

    @Bean
    PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

}
