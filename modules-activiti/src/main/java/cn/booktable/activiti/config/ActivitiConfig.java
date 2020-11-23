package cn.booktable.activiti.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "activiti")
@Data
public class ActivitiConfig {

    private String jdbcUrl;
    private String jdbcDriver;
    private String jdbcPassword;
    private String jdbcUsername;

}
