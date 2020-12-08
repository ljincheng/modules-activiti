package cn.booktable.activiti.core;

import cn.booktable.activiti.service.activiti.impl.ActApproveEventHandlerImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActConfiguration {

    @Bean
    @ConditionalOnMissingBean(ActApproveEventHandler.class)
    public ActApproveEventHandler actApproveEventHandler(){
        return new ActApproveEventHandlerImpl();
    }
}
