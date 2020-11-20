package cn.booktable.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;

public class ActivitiRuntimeTest extends BaseTest {


    @Test
    public void startProcess(){
        log.info("##### 第二步：{} ####","执行工作流");

        String deploymentId="1";

        ProcessEngine processEngine = getProcessEngine();

        RepositoryService repositoryService = processEngine.getRepositoryService();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
        System.out.println("流程名称 ： [" + processDefinition.getName() + "]， 流程ID ： ["
                + processDefinition.getId() + "], 流程KEY : " + processDefinition.getKey());


        // 启动流程
        RuntimeService runtimeService = processEngine.getRuntimeService();

        //指定执行我们刚才部署的工作流程
        String processDefiKey=PROCESS_DEFINITION_KEY;
//        //取运行时服务
//        RuntimeService runtimeService = processEngine.getRuntimeService();
//        //取得流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefiKey,"defaultServiceTaskBehavior");//通过流程定义的key 来执行流程
        System.out.println("流程实例id:"+pi.getId());//流程实例id
        System.out.println("流程定义id:"+pi.getProcessDefinitionId());//输出流程定义的id
    }

}
