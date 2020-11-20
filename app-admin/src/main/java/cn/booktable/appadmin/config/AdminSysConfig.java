package cn.booktable.appadmin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author ljc
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "booktable.sys")
@Data
public class AdminSysConfig {

    /**
     * 密码KEY
     *
     */
    private String passwordKey;

    /**
     * 附件根目录
     */
    private String attachmentRoot;

    /**
     * 头像访问地址
     */
    private String avatarHost;

    /**
     * 头像保存路径
     */
    private String avatarSavePath;
}
