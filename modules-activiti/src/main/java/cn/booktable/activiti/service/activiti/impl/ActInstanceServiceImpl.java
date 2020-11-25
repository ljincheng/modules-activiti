package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.*;
import cn.booktable.activiti.service.activiti.ActInstanceMetaInfoHelper;
import cn.booktable.activiti.service.activiti.ActInstanceService;
import cn.booktable.activiti.utils.ActivitiUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("actInstanceService")
public class ActInstanceServiceImpl implements ActInstanceService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Override
    public List<ActInstance> queryListAll(String approvalCode,String deploymentId) {

        List<ActInstance> result=new ArrayList<>();
        ProcessInstanceQuery query= runtimeService.createProcessInstanceQuery();
        if(approvalCode!=null && approvalCode.length()>0)
        {
            query.processDefinitionKey(approvalCode);
        }
        if(deploymentId!=null && deploymentId.length()>0)
        {
            query.deploymentId(deploymentId);
        }
        List<ProcessInstance> lstPis = query.list();
        if(lstPis!=null){
            for(int i=0,k=lstPis.size();i<k;i++)
            {
                ProcessInstance instance=lstPis.get(i);
                ActInstance actInstance=new ActInstance();
                actInstance.setApprovalCode(instance.getProcessDefinitionKey());
                actInstance.setInstanceCode(instance.getBusinessKey());
                actInstance.setApprovalName(instance.getProcessDefinitionName());
                actInstance.setInstanceName(instance.getName());
                actInstance.setStartTime(instance.getStartTime());
                actInstance.setDeploymentId(instance.getDeploymentId());
                result.add(actInstance);
            }
        }
        return result;
    }

    @Override
    public ActInstance detail(String instanceCode) {

        ActInstance actInstance = new ActInstance();


        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        ProcessInstance processInstance = query.processInstanceBusinessKey(instanceCode).singleResult();
        if (processInstance != null) {
//             runtimeServic
            actInstance.setInstanceCode(instanceCode);
            actInstance.setStartTime(processInstance.getStartTime());
            actInstance.setDeploymentId(processInstance.getDeploymentId());
            actInstance.setApprovalCode(processInstance.getProcessDefinitionKey());
            actInstance.setApprovalName(processInstance.getProcessDefinitionName());
            actInstance.setInstanceName(processInstance.getName());
            actInstance.setUserId(processInstance.getStartUserId());
            actInstance.setId(processInstance.getId());


            List<Task> taskList = taskService.createTaskQuery().processInstanceId(actInstance.getId()).processInstanceBusinessKey(instanceCode).list();
            List<ActTask> actTaskList = new ArrayList<>();
            actInstance.setTaskList(actTaskList);
            if (taskList != null) {
                for (int i = 0, k = taskList.size(); i < k; i++) {
                    Task task = taskList.get(i);
                    ActTask actTask = new ActTask();
                    actTask.setId(task.getId());
                    actTask.setName(task.getName());
                    actTask.setOwner(task.getOwner());
                    actTask.setAssignee(task.getAssignee());
                    actTask.setCategory(task.getCategory());
                    actTask.setClaimTime(task.getClaimTime());
                    actTask.setCreateTime(task.getCreateTime());
                    actTask.setDescription(task.getDescription());
                    actTask.setDueDate(task.getDueDate());
                    actTask.setExecutionId(task.getExecutionId());
                    actTask.setFormKey(task.getFormKey());
                    actTask.setProcessVariables(task.getProcessVariables());
                    actTask.setTaskLocalVariables(task.getTaskLocalVariables());
                    actTask.setTenantId(task.getTenantId());
                    actTask.setParentTaskId(task.getParentTaskId());
                    actTask.setProcessDefinitionId(task.getProcessDefinitionId());
                    actTask.setProcessInstanceId(task.getProcessInstanceId());
                    actTaskList.add(actTask);
                }
            }
        }else {
             HistoricProcessInstance historicProcessInstance= historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(instanceCode).singleResult();
             if(historicProcessInstance!=null){
                 actInstance.setInstanceCode(instanceCode);
                 actInstance.setStartTime(historicProcessInstance.getStartTime());
                 actInstance.setApprovalName(historicProcessInstance.getName());
                 actInstance.setApprovalCode(historicProcessInstance.getProcessDefinitionKey());
                 actInstance.setUserId(historicProcessInstance.getStartUserId());
                 actInstance.setId(historicProcessInstance.getId());

             }else {
                 return null;
             }
        }


        List<Comment>  commentList=  taskService.getProcessInstanceComments(actInstance.getId());
        List<ActComment> actCommentList=new ArrayList<>();
        actInstance.setCommentList(actCommentList);
        if(commentList!=null)
        {
            for(int i=0,k=commentList.size();i<k;i++){
                Comment comment=commentList.get(i);

                ActComment actComment=new ActComment();
                actComment.setComment(comment.getFullMessage());
                actComment.setId(comment.getId());
                actComment.setUserId(comment.getUserId());
                actComment.setCreateTime(comment.getTime());
                actCommentList.add(actComment);
            }

        }
        Task task=taskService.createTaskQuery().processInstanceId(actInstance.getId()).singleResult();
        if(task!=null) {
            Map<String, Object> instanceVar = taskService.getVariables(task.getId());
            ActInstanceMetaInfo metaInfo = ActInstanceMetaInfoHelper.getMetaInfo(instanceVar);
            if (metaInfo != null) {
                actInstance.setInstanceName(metaInfo.getName());
            }
        }

//        query.in
        return actInstance;
    }


    @Override
    public void approve(String taskId, String instanceCode, String comments, String userId, Map<String, Object> variables) {

        // 使用任务ID，查询任务对象，获取流程流程实例ID
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(instanceCode)
                .taskId(taskId)// 使用任务ID查询
                .singleResult();

        // 获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        /**
         * 注意：添加批注的时候，由于Activiti底层代码是使用： String userId =
         * Authentication.getAuthenticatedUserId(); CommentEntity comment = new
         * CommentEntity(); comment.setUserId(userId);
         * 所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，不过不添加审核人，该字段为null
         * 所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
         */
        Authentication.setAuthenticatedUserId(userId);
        taskService.addComment(taskId, processInstanceId, comments);


        // 使用任务ID，完成当前人的个人任务，同时流程变量
        taskService.complete(taskId, variables, true);
        /**
         * 在完成任务之后，判断流程是否结束 如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
         */
    }



    @Override
    public ActResult<String> create(String approvalCode, String instanceCode, String userId, String name, Map<String, Object> variables) {
        ActResult<String> result=new ActResult<>();
        long num= runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(instanceCode).processDefinitionKey(approvalCode).count();
        if(num>0){
            ActivitiUtils.setFailResult(result,"审批实例编码已被使用过");
            return result;
        }
        Authentication.setAuthenticatedUserId(userId);
        Map<String ,Object> instanceVariables= ActInstanceMetaInfoHelper.createInstanceVariables(name,userId,null,"START",variables);
        ProcessInstance processInstance= runtimeService.startProcessInstanceByKey(approvalCode, instanceCode, instanceVariables);
        if(processInstance!=null) {
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).processInstanceBusinessKey(instanceCode).list();
            if(taskList!=null && taskList.size()==1) {
                approve(taskList.get(0).getId(), instanceCode, "提交申请", userId, variables);
            }
            ActivitiUtils.setOkResult(result);
        }else {
            ActivitiUtils.setFailResult(result,"发起实例失败");
        }
        return result;
    }
}


