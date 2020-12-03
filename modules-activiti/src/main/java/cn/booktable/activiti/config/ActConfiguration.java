package cn.booktable.activiti.config;

import cn.booktable.activiti.service.activiti.impl.ActivitiUserDetailsManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableAutoConfiguration
public class ActConfiguration {

    @Bean
    public UserDetailsService activitiUserDetailsService() {

        return new ActivitiUserDetailsManager(null);
    }
}
