package cn.booktable.appadmin;

//import org.activiti.core.common.spring.security.config.ActivitiSpringSecurityAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class})
// @SpringBootApplication(exclude = ActivitiSpringSecurityAutoConfiguration.class)
@MapperScan("cn.booktable.modules.dao")
@ComponentScan("cn.booktable")
//@EnableFeignClients
//@EnableAutoConfiguration
public class AppAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppAdminApplication.class, args);
	}

}
