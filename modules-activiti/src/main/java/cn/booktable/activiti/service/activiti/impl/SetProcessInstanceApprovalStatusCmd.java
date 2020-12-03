package cn.booktable.activiti.service.activiti.impl;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.runtime.ProcessInstance;

import java.io.Serializable;

public class SetProcessInstanceApprovalStatusCmd implements Command<Void>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String processInstanceId;
    private final String approvalStatus;

    public SetProcessInstanceApprovalStatusCmd(String processInstanceId, String approvalStatus) {
        if (processInstanceId == null || processInstanceId.length() < 1) {
            throw new ActivitiIllegalArgumentException("The process instance id is mandatory, but '" + processInstanceId + "' has been provided.");
        }
        if (approvalStatus == null) {
            throw new ActivitiIllegalArgumentException("The business key is mandatory, but 'null' has been provided.");
        }

        this.processInstanceId = processInstanceId;
        this.approvalStatus = approvalStatus;
    }

    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionManager = commandContext.getExecutionEntityManager();
        ExecutionEntity processInstance = executionManager.findById(processInstanceId);
        if (processInstance == null) {
            throw new ActivitiObjectNotFoundException("No process instance found for id = '" + processInstanceId + "'.", ProcessInstance.class);
        } else if (!processInstance.isProcessInstanceType()) {
            throw new ActivitiIllegalArgumentException("A process instance id is required, but the provided id " + "'" + processInstanceId + "' " + "points to a child execution of process instance " + "'"
                    + processInstance.getProcessInstanceId() + "'. " + "Please invoke the " + getClass().getSimpleName() + " with a root execution id.");
        }

        processInstance.setDescription(this.approvalStatus);
        HistoricProcessInstanceEntity historicProcessInstance = commandContext.getHistoricProcessInstanceEntityManager().findById(processInstanceId);
        if (historicProcessInstance != null) {
            historicProcessInstance.setDescription(approvalStatus);
            commandContext.getHistoricProcessInstanceEntityManager().update(historicProcessInstance,false);
        }
        executionManager.update(processInstance);
        return null;
    }
}
