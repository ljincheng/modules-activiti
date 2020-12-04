package cn.booktable.activiti.core;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
System.out.println("=====delegateTask="+ JSON.toJSONString(delegateTask));
        String processId= delegateTask.getProcessDefinitionId();
       System.out.println("ProcessId="+processId);
        String instanceId= delegateTask.getProcessInstanceId();


        if("N2".equals(delegateTask.getTaskDefinitionKey())){
            delegateTask.addCandidateUser("114");
            delegateTask.addCandidateUser("-1");
        }else {
            delegateTask.setAssignee("114");
        }
    }
}
