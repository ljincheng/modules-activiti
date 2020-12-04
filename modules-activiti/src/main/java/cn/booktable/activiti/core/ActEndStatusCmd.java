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

public class ActEndStatusCmd implements Command<Void> {

    private String taskId;
    private String status;

    public ActEndStatusCmd(String taskId,String status){
        this.taskId=taskId;
        this.status=status;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(this.taskId);
        //获取历史管理
        HistoryManager historyManager = commandContext.getHistoryManager();
        //获取当前节点的执行实例
        ExecutionEntity execution = taskEntity.getExecution();
        //通知当前活动结束(更新act_hi_actinst)
        historyManager.recordActivityEnd(execution,status);
        //通知任务节点结束(更新act_hi_taskinst)
        historyManager.recordTaskEnd(taskId,status);

        return null;
    }
}
