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





 //   @Test
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

}
