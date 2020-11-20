package cn.booktable.appactiviti.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(ActivitiConfig.class)
public class ActivitiAutoConfig {

    @Resource
    private ActivitiConfig activitiConfig;
}
