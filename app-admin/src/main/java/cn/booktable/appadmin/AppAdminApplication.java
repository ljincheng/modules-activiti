package cn.booktable.appadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication
@MapperScan("cn.booktable.modules.dao")
@ComponentScan("cn.booktable")
//@EnableFeignClients
@EnableAutoConfiguration
public class AppAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppAdminApplication.class, args);
	}

}
