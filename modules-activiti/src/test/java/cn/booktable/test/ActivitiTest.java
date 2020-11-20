package cn.booktable.test;

import cn.booktable.appactiviti.config.ActivitiConfig;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import javax.annotation.Resource;

@TestConfiguration
public class ActivitiTest extends BaseTest {


    @Resource
    private ActivitiConfig activitiConfig;

    @Test
    public void hello(){
        log.info("================ Hello word! =============");
        log.info("================activiti: url"+activitiConfig.getJdbcUrl());
     //   log.info("DataSource="+dataSource.toString());
    }

//    @Test
    public void testActiviEngine(){
        log.info("engine test");
    }


    @Test
    public void createActivitiEngine(){

        /*        *1.通过代码形式创建
         *  - 取得ProcessEngineConfiguration对象
         *  - 设置数据库连接属性
         *  - 设置创建表的策略 （当没有表时，自动创建表）
         *  - 通过ProcessEngineConfiguration对象创建 ProcessEngine 对象*/

        //取得ProcessEngineConfiguration对象
        ProcessEngineConfiguration engineConfiguration=ProcessEngineConfiguration.
                createStandaloneProcessEngineConfiguration();
        //设置数据库连接属性
//        engineConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
//        engineConfiguration.setJdbcUrl("jdbc:mysql://localhost:3306/activitiDB?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8");
//        engineConfiguration.setJdbcUsername("root");
//        engineConfiguration.setJdbcPassword("root");

        engineConfiguration.setJdbcDriver(activitiConfig.getJdbcDriver());
        engineConfiguration.setJdbcUrl(activitiConfig.getJdbcUrl());
        engineConfiguration.setJdbcUsername(activitiConfig.getJdbcUsername());
        engineConfiguration.setJdbcPassword(activitiConfig.getJdbcPassword());


        // 设置创建表的策略 （当没有表时，自动创建表）
        //		  public static final java.lang.String DB_SCHEMA_UPDATE_FALSE = "false";//不会自动创建表，没有表，则抛异常
        //		  public static final java.lang.String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";//先删除，再创建表
        //		  public static final java.lang.String DB_SCHEMA_UPDATE_TRUE = "true";//假如没有表，则自动创建
        engineConfiguration.setDatabaseSchemaUpdate("true");
        //通过ProcessEngineConfiguration对象创建 ProcessEngine 对象
        ProcessEngine processEngine = engineConfiguration.buildProcessEngine();
        System.out.println("流程引擎创建成功!");

    }


    @Test
    public void createActivitiEngineFromResource(){

        /**2. 通过加载 activiti.cfg.xml 获取 流程引擎 和自动创建数据库及表
         */

         ProcessEngineConfiguration engineConfiguration=
         ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti-conf.xml");
         //从类加载路径中查找资源  activiti.cfg.xm文件名可以自定义
         ProcessEngine processEngine = engineConfiguration.buildProcessEngine();
         System.out.println("使用配置文件activiti-conf.xml获取流程引擎");


    }

    @Test
    public void initActivitiEngine(){
        /**
         * 3. 通过ProcessEngines 来获取默认的流程引擎
         */
        //  默认会加载类路径下的 activiti.cfg.xml
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        System.out.println("通过ProcessEngines 来获取流程引擎");
    }





}
