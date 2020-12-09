package cn.booktable.activiti.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.activiti.actconfig")
public class ActConfig {
    private String approvalTemplate;
}
