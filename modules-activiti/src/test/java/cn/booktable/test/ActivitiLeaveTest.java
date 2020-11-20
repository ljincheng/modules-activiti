package cn.booktable.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiLeaveTest extends BaseTest {


    @Test
    public void deploy() {

        log.info("##### 第一步：{} ####","工作流部署到工作流引擎中了");
        ProcessEngine processEngine = getProcessEngine();
        System.out.println("通过ProcessEngines 来获取流程引擎");
        //获取仓库服务 ：管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()//创建一个部署的构建器
                .addClasspathResource("processes/leave.bpmn")//从类路径中添加资源,一次只能添加一个资源
                .name("请假流程")//设置部署的名称
                .category("ERP")//设置部署的类别
                .deploy();

        System.out.println("部署的id"+deploy.getId());
        //部署的id7501
        //部署的名称请假流程
        System.out.println("部署的名称"+deploy.getName());
    }


    @Test
    public void getprocesslists(){

        log.info("========== {} ==========","流程列表");
        ProcessEngine processEngine = getProcessEngine();
        RepositoryService rep = processEngine.getRepositoryService();
        List<ProcessDefinition> list = rep.createProcessDefinitionQuery().listPage(0, 100);
        int total = rep.createProcessDefinitionQuery().list().size();
        for (int i = 0; i < list.size(); i++) {
            ProcessDefinition p = list.get(i);
            log.info("ProcessDef: deployId={},id={},key={},name={},resourceName={},diagramReesourceName={}",p.getDeploymentId(),p.getId(),p.getKey(),p.getName(),p.getResourceName(),p.getDiagramResourceName());

        }
    }

    @Test
    public void startPro(){
        //指定执行我们刚才部署的工作流程
        log.info("========== {} ==========","发起请假");
        String processDefiKey="7501";
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applyuserid", "114");
        ProcessEngine processEngine = getProcessEngine();
        RuntimeService runtimeservice = processEngine.getRuntimeService();
        ProcessInstance instance=runtimeservice.startProcessInstanceByKey("leave",processDefiKey,variables);
        String instanceid=instance.getId();
        log.info("instanceid={}",instanceid);//instanceid=10001
    }

    public  void  showresource(){
        log.info("========== {} ==========","流程图");
        String pdid="7501";//请假流程
        ProcessEngine processEngine = getProcessEngine();
        RepositoryService rep = processEngine.getRepositoryService();
        ProcessDefinition def = rep.createProcessDefinitionQuery().processDefinitionId(pdid).singleResult();
        InputStream is = rep.getResourceAsStream(def.getDeploymentId(), "/workspace/temp/activiti-leave.png");
//        ServletOutputStream output = response.getOutputStream();
//        IOUtils.copy(is, output);
    }
}
