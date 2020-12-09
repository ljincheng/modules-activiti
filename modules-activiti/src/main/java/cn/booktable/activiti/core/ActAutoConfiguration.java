package cn.booktable.activiti.core;


import cn.booktable.activiti.service.activiti.impl.ActApproveEventHandlerImpl;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(ActConfig.class)
public class ActAutoConfiguration {

    @Resource
    private  ActConfig actConfig;

    @Bean
    @ConditionalOnMissingBean(ActApproveEventHandler.class)
    public ActApproveEventHandler actApproveEventHandler(){
        return new ActApproveEventHandlerImpl();
    }


    @Bean
    public ActApprovalTemplate actFormTemplate(){
        try {
            ActApprovalTemplate op =null;
            String tmlRes=actConfig.getApprovalTemplate();
            if(tmlRes!=null && tmlRes.length()>0){
                org.springframework.core.io.Resource resource = new ClassPathResource(tmlRes);
                InputStreamReader in = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                String templateJson = IOUtils.toString(in);
//                ObjectMapper mapper = new ObjectMapper();
//                op=mapper.convertValue(templateJson,ActApprovalTemplate.class);
                op= JSONObject.parseObject(templateJson,ActApprovalTemplate.class);
            }else {
                op=new ActApprovalTemplate();
            }
            return op;
        }catch (IOException ex)
        {
            throw new ActException(ex);
        }
    }
}
