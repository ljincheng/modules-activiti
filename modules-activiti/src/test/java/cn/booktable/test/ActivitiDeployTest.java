package cn.booktable.test;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivitiDeployTest extends BaseTest {




//    @Autowired
    private ProcessRuntime processRuntime;


    @Test
    public void contextLoads() {
//        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        ProcessDefinition processDefinition = processRuntime.processDefinition(PROCESS_DEFINITION_KEY);

        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDefinition.getAppVersion()).isNull();
    }

   @Test
    public void deploy() {

        log.info("##### 第一步：{} ####","工作流部署到工作流引擎中了");
        ProcessEngine processEngine = getProcessEngine();
        System.out.println("通过ProcessEngines 来获取流程引擎");
        //获取仓库服务 ：管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()//创建一个部署的构建器
                .addClasspathResource("processes/categorize-content.bpmn20.xml")//从类路径中添加资源,一次只能添加一个资源
                .name("请求单流程")//设置部署的名称
                .category("办公类别")//设置部署的类别
                .deploy();

        System.out.println("部署的id"+deploy.getId());
        System.out.println("部署的名称"+deploy.getName());
    }




}
