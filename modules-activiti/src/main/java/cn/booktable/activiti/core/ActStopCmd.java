package cn.booktable.activiti.core;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.Collection;

public class ActStopCmd implements Command<Void> {

    private String taskId;

    public ActStopCmd(String taskId){
        this.taskId=taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(this.taskId);

        ExecutionEntity executionEntity = executionEntityManager.findById(taskEntity.getExecutionId());

        Process process = ProcessDefinitionUtil.getProcess(executionEntity.getProcessDefinitionId());

        Collection<FlowElement> flowElements = process.getFlowElements();
        FlowElement endFlow=null;
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof EndEvent) {
                endFlow=flowElement;
                break;
            }
        }



//        taskEntityManager.deleteTask(taskEntity, "中止操作", true, true);
        //获取历史管理
        HistoryManager historyManager = commandContext.getHistoryManager();
        //获取当前节点的执行实例
        ExecutionEntity execution = taskEntity.getExecution();
        //通知当前活动结束(更新act_hi_actinst)
        historyManager.recordActivityEnd(execution,"jump to end");
        //通知任务节点结束(更新act_hi_taskinst)
        historyManager.recordTaskEnd(taskId,"jump to end");

        //taskEntityManager.deleteTask(taskEntity, "中止操作", true, true);


        //  FlowElement targetFlowElement = process.getFlowElement(targetNodeId);
        executionEntity.setCurrentFlowElement(endFlow);

        ActivitiEngineAgenda agenda = commandContext.getAgenda();
//        agenda.planContinueProcessInCompensation(executionEntity);
        agenda.planEndExecutionOperation(executionEntity);
        return null;
    }
}
