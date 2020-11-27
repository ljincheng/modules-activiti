package cn.booktable.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author ljc
 */
@ExtendWith(SpringExtension.class)
//@ComponentScan("cn.booktable")
//@ContextConfiguration(classes = ElasticsearchAutoConfiguration.class)
//@EnableConfigurationProperties(value = ActivitiConfig.class)
@TestPropertySource("classpath:config-test.properties")
public class BaseTest {

    protected static Logger log= LoggerFactory.getLogger(BaseTest.class);

    public final String PROCESS_DEFINITION_KEY = "categorizeProcess";

    protected ProcessEngine getProcessEngine(){

        /**2. 通过加载 activiti.cfg.xml 获取 流程引擎 和自动创建数据库及表
         */

        ProcessEngineConfiguration engineConfiguration=
                ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti-conf.xml");
        //从类加载路径中查找资源  activiti.cfg.xm文件名可以自定义
        ProcessEngine processEngine = engineConfiguration.buildProcessEngine();
        System.out.println("使用配置文件activiti-conf.xml获取流程引擎");

        return processEngine;

    }
}
