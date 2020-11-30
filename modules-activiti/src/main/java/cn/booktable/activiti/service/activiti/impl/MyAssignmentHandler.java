package cn.booktable.activiti.service.activiti.impl;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String processId= delegateTask.getProcessDefinitionId();
       System.out.println("ProcessId="+processId);
        String instanceId= delegateTask.getProcessInstanceId();

       delegateTask.setAssignee("114");
    }
}
