package cn.booktable.activiti.config;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(ActivitiConfig.class)
public class ActivitiAutoConfiguration {

    @Resource
    private ActivitiConfig activitiConfig;

//    @Bean(name = "processEngineConfiguration")
//    public ProcessEngineConfiguration getProcessEngineConfiguration(){
//        ProcessEngineConfiguration engineConfiguration=ProcessEngineConfiguration.
//                createStandaloneProcessEngineConfiguration();
//        engineConfiguration.setJdbcDriver(activitiConfig.getJdbcDriver());
//        engineConfiguration.setJdbcUrl(activitiConfig.getJdbcUrl());
//        engineConfiguration.setJdbcUsername(activitiConfig.getJdbcUsername());
//        engineConfiguration.setJdbcPassword(activitiConfig.getJdbcPassword());
////        engineConfiguration.setDatabaseSchemaUpdate("true");
//        engineConfiguration.setDatabaseSchemaUpdate("create-drop");
////        ProcessEngine processEngine = engineConfiguration.buildProcessEngine();
////        return processEngine;
//        return engineConfiguration;
//    }


}
