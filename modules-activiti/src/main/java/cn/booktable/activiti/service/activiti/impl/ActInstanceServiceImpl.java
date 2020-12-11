package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.core.*;
import cn.booktable.activiti.entity.activiti.*;
import cn.booktable.activiti.service.activiti.ActInstanceService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.activiti.utils.AssertUtils;
import cn.booktable.core.page.PageDo;
import cn.booktable.util.StringUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service("actInstanceService")
public class ActInstanceServiceImpl implements ActInstanceService {
    private static Logger logger= LoggerFactory.getLogger(ActInstanceServiceImpl.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;
    @Autowired
    private ProcessDiagramGenerator processDiagramGenerator;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private ActApproveEventHandler actApproveEventHandler;

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
                ActivitiUtils.parseInstance(instance,actInstance);
                result.add(actInstance);
            }
        }
        return result;
    }

    @Override
    public ActInstance detail(String instanceCode) {

        ActInstance actInstance = new ActInstance();


        String processDefinitionId=null;
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        ProcessInstance processInstance = query.processInstanceBusinessKey(instanceCode).singleResult();
        if (processInstance != null) {
            processDefinitionId=processInstance.getProcessDefinitionId();
            ActivitiUtils.parseInstance(processInstance,actInstance);

            List<Task> taskList = taskService.createTaskQuery().processInstanceId(actInstance.getId()).processInstanceBusinessKey(instanceCode).list();
            List<ActTask> actTaskList = new ArrayList<>();
            actInstance.setTaskList(actTaskList);
            if (taskList != null) {
                for (int i = 0, k = taskList.size(); i < k; i++) {
                    Task task = taskList.get(i);
                    ActTask actTask =ActivitiUtils.parseTask(task);
                    actTaskList.add(actTask);
                }
            }
            Object formObj= runtimeService.getVariable(processInstance.getId(),ActivitiUtils.INSTANCE_VAR_FORM);
            if(formObj!=null){
                actInstance.setForm(ActivitiUtils.convertJsonObjectToMap(formObj));
            }
        }


        HistoricProcessInstance historicProcessInstance= historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processInstanceBusinessKey(instanceCode).singleResult();
        if(historicProcessInstance!=null){
            processDefinitionId=historicProcessInstance.getProcessDefinitionId();
            ActivitiUtils.parseInstance(historicProcessInstance,actInstance);
            List<HistoricVariableInstance> formList=historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).variableName(ActivitiUtils.INSTANCE_VAR_FORM).list();
            if(formList!=null && formList.size()>0){
                Object formObj=formList.get(0).getValue();
                actInstance.setForm(ActivitiUtils.convertJsonObjectToMap(formObj));
            }
            if(processInstance!=null){
                if(processInstance.isSuspended()){
                    actInstance.setStatus(ActStatus.INSTANCE_CANCELED);
                }else {
                    actInstance.setStatus(ActStatus.INSTANCE_PENDING);
                }
            }else{
                if(actInstance.getStatus()==null || actInstance.getStatus().length()==0){
                    actInstance.setStatus(ActStatus.INSTANCE_DONE);
                }
            }

        }else {
            return null;
        }

//        HistoricActivityInstanceQuery historyInstanceQuery = historyService.createHistoricActivityInstanceQuery().processInstanceId(actInstance.getId());
        // 查询历史节点
//        List<HistoricActivityInstance> historicActivityInstanceList = historyInstanceQuery.orderByHistoricActivityInstanceStartTime().asc().list();
//        logger.debug("historicActivityInstanceList="+JSONObject.toJSONString(historicActivityInstanceList));

        List<Comment>  commentList=  taskService.getProcessInstanceComments(actInstance.getId());
        List<ActComment> actCommentList=new ArrayList<>();
        actInstance.setCommentList(actCommentList);
        if(commentList!=null)
        {
            if(ActStatus.INSTANCE_CANCELED.equals(actInstance.getStatus())){

                HistoricVariableInstance canceledInstance= historyService.createHistoricVariableInstanceQuery().processInstanceId(actInstance.getId()).variableValueEquals(ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS,ActStatus.INSTANCE_CANCELED).singleResult();
                if(canceledInstance!=null) {
                    actInstance.setEndTime(canceledInstance.getTime());
                    ActComment canceledComment = new ActComment();
                    canceledComment.setStatusName(ActivitiUtils.approvalStatusToName(ActStatus.INSTANCE_CANCELED));
                    canceledComment.setStatus(ActStatus.INSTANCE_CANCELED);
                    canceledComment.setComment("[" + canceledComment.getStatus() + "]" + canceledComment.getStatusName());
                    canceledComment.setUserId(actInstance.getUserId());
                    canceledComment.setCreateTime(canceledInstance.getTime());
                    canceledComment.setMessage(canceledComment.getStatusName());
                    canceledComment.setNodeName("提交");
                    actCommentList.add(canceledComment);
                }
            }
            for(int i=0,k=commentList.size();i<k;i++){
                Comment comment=commentList.get(i);

                String fullMsg=comment.getFullMessage();
                if(fullMsg!=null && fullMsg.length()>0 && fullMsg.charAt(0)=='{'){
                    ObjectMapper objectMapper=new ObjectMapper();
                    try {
                        ActComment actComment = objectMapper.readValue(comment.getFullMessage(), ActComment.class);
                        actComment.setStatusName(ActivitiUtils.approvalStatusToName(actComment.getStatus()));
                        actComment.setCreateTime(comment.getTime());
                        actCommentList.add(actComment);
                    }catch (JsonParseException ex){
                        throw new ActException(ex);
                    }catch (IOException ex2){
                        throw new ActException(ex2);
                    }
                }else {
                    ActComment actComment = new ActComment();
                    actComment.setComment(comment.getFullMessage());
                    actComment.setId(comment.getId());
                    actComment.setUserId(comment.getUserId());
                    actComment.setCreateTime(comment.getTime());
                    actComment.setTaskId(comment.getTaskId());
                    actComment.setStatus(ActivitiUtils.calculateCommentStatus(comment));
                    actComment.setStatusName(ActivitiUtils.approvalStatusToName(actComment.getStatus()));
                    String fullMessage = comment.getFullMessage();
                    if (fullMessage.startsWith("[") && fullMessage.indexOf("]") > 0) {
                        int endStatusIndex = fullMessage.indexOf("]");
                        String status = fullMessage.substring(1, endStatusIndex);
                        actComment.setStatus(status);
                        actComment.setMessage(fullMessage.substring(endStatusIndex + 1));
                    } else {
                        actComment.setMessage(fullMessage);
                    }
                    actCommentList.add(actComment);
                }
            }

        }
        ActComment startComment=new ActComment();
        startComment.setStatus(ActStatus.START);
        startComment.setStatusName(ActivitiUtils.approvalStatusToName(ActStatus.START));
        startComment.setComment("["+startComment.getStatus()+"]"+startComment.getStatusName());
        startComment.setUserId(actInstance.getUserId());
        startComment.setCreateTime(actInstance.getStartTime());
        startComment.setMessage("提交");
        startComment.setNodeName("提交");
        actCommentList.add(startComment);

//        List<ActTimeline> historyTimelines=new ArrayList<>();
        List<ActTimeline> timelines=new ArrayList<>();
        actInstance.setTimelineList(timelines);
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        if(model != null) {
            Collection<FlowElement>  flowElements = model.getMainProcess().getFlowElements();
            for(FlowElement e : flowElements) {
                ActTimeline timeline=getActTimelineFromFlow(e);
                if(timeline!=null){
                    timeline.setComments(findTaskComment(timeline.getTaskId(),actCommentList));
                    timelines.add(timeline);
                }
            }
        }



        return actInstance;
    }

    private List<ActComment> findTaskComment(String taskId,List<ActComment> commentList){
        List<ActComment> result=null;
        if(commentList!=null && taskId!=null && taskId.length()>0){
            for(int i=0,k=commentList.size();i<k;i++){
                ActComment comment=commentList.get(i);
                if(taskId.equals(comment.getTaskId())){
                    if(result==null){
                        result=new ArrayList<>();
                    }
                    result.add(comment);
                }
            }
        }
        return result;
    }

    private ActTimeline getActTimelineFromFlow(FlowElement e){
        ActTimeline timeline=null;
        if(e !=null && e instanceof UserTask)
        {
            UserTask userTask=(UserTask)e;

            timeline=new ActTimeline();
            timeline.setId(e.getId());
            timeline.setName(e.getName());
            timeline.setUserId(userTask.getAssignee());
            timeline.setGroups(userTask.getCandidateGroups());
            timeline.setUsers(userTask.getCandidateUsers());
//            List<ActivitiListener> taskListenersList= userTask.getTaskListeners();
//            if(taskListenersList!=null && taskListenersList.size()>0)
//            {
//                for(int i=0,k=taskListenersList.size();i<k;i++){
//                    ActivitiListener listener=taskListenersList.get(i);
//                    String event= listener.getEvent();
//                    System.out.println("Event="+event);
//                }
//
//            }

            //查看流向节点
            List<SequenceFlow> outgoFlowList= userTask.getOutgoingFlows();
            if(outgoFlowList!=null && outgoFlowList.size()>0){
                List<ActTimeline> outgoingList=new ArrayList<>();
                timeline.setOutgoing(outgoingList);

                for(int i=0,k=outgoFlowList.size();i<k;i++){
                    SequenceFlow outFlow=outgoFlowList.get(i);
                   List<ActTimeline> outTls= getOutgoingFlow(outFlow);
                   if(outTls!=null && outTls.size()>0) {
                       outgoingList.addAll(outTls);
                   }
                }
            }
        }
        logger.debug("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
//        System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
        return timeline;
    }

    private List<ActTimeline> getOutgoingFlow(SequenceFlow outFlow){
        List<ActTimeline> result=null;
        FlowElement targetFlow= outFlow.getTargetFlowElement();
        if(targetFlow instanceof UserTask) {
            result=new ArrayList<>();
            UserTask outUserTask=(UserTask)targetFlow;
            ActTimeline targetTimeline=new ActTimeline();
            targetTimeline.setId(targetFlow.getId());
            targetTimeline.setName(targetFlow.getName());
            targetTimeline.setUserId(outUserTask.getAssignee());
            targetTimeline.setGroups(outUserTask.getCandidateGroups());
            targetTimeline.setUsers(outUserTask.getCandidateUsers());
            result.add(targetTimeline);
        }else if(targetFlow instanceof ExclusiveGateway){
            ExclusiveGateway gateway=(ExclusiveGateway)targetFlow;
            List<SequenceFlow> subFlows=gateway.getOutgoingFlows();
            if(subFlows!=null && subFlows.size()>0) {
                result=new ArrayList<>();
                for (int i = 0, k = subFlows.size(); i < k; i++) {
                    SequenceFlow subFlow = subFlows.get(i);
                    List<ActTimeline> subresult = getOutgoingFlow(subFlow);
                    if(subresult!=null && subresult.size()>0)
                    {
                        result.addAll(subresult);
                    }
                }
            }
        }else if(targetFlow instanceof EndEvent){
            if(result==null) {
                result = new ArrayList<>();
            }
            ActTimeline targetTimeline=new ActTimeline();
            targetTimeline.setId(targetFlow.getId());
            targetTimeline.setName("结束节点");
            result.add(targetTimeline);
        }
        return result;
    }


    @Override
    public ActResult<Void> approve(String taskId, String instanceCode,String status, String comments, String userId, Map<String, Object> variables) {
        ActResult<Void> result=new ActResult<>();
        AssertUtils.isNotBlank(instanceCode, ActErrorCodeEnum.EMPTY_INSTANCECODE);
        AssertUtils.isNotBlank(status, ActErrorCodeEnum.EMPTY_APPROVALSTATUS);
        AssertUtils.hasIn(status, ActErrorCodeEnum.EXCLUDE_APPROVALSTATUS,ActStatus.INSTANCE_APPROVED,ActStatus.INSTANCE_REJECTED,ActStatus.INSTANCE_CANCELED,ActStatus.INSTANCE_DELETED);
        Task task =null;
        ProcessInstance processInstance =null;
        String processInstanceId =null;
        boolean isGroupUserTask=false;//是否是用户组
        // 使用任务ID，查询任务对象，获取流程流程实例ID
        securityUtil.logInAs(userId);
        processInstance=runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(instanceCode).singleResult();
        AssertUtils.notNull(processInstance,ActErrorCodeEnum.UNEXIST_INSTANCE);
        processInstanceId=processInstance.getId();
        boolean suspended = processInstance.isSuspended();
        AssertUtils.isTrue(!suspended,ActErrorCodeEnum.INSTANCE_SUSPENDED);


        if(status.equals(ActStatus.INSTANCE_CANCELED)){
            AssertUtils.isTrue(userId.equals(processInstance.getStartUserId()),ActErrorCodeEnum.UNCANCELED_INSTANCE_OWNER);
        }else{
            AssertUtils.isNotBlank(taskId, ActErrorCodeEnum.EMPTY_TASKID);
            TaskQuery taskQuery=taskService.createTaskQuery().processInstanceBusinessKey(instanceCode).active().taskId(taskId);
            long num=taskQuery.count();
            AssertUtils.isTrue(num>0, ActErrorCodeEnum.UNEXIST_ACTIVETASK);
            task =taskQuery.singleResult();
            String taskUserId=task.getAssignee();
            if(taskUserId!=null && taskUserId.length()>0){//单个审批人
                AssertUtils.isTrue((ActStatus.START.equals(status) || taskUserId.equals(userId)), ActErrorCodeEnum.INVALID_APPROVALUSER);
            }else {
                List<Task> groupTask=taskService.createTaskQuery().taskCandidateUser(userId).taskId(taskId).list();
                if(groupTask!=null && groupTask.size()>0){
                    task=groupTask.get(0);
                    isGroupUserTask=true;
                }else  if(!ActStatus.START.equals(status)) {
                    ActivitiUtils.setFailResult(result, "当前流程节点没有设置审批人");
                    return result;
                }
            }
            //　处理审批操作意见
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
            ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
            FlowElement currentFlowEl = bpmnModel.getFlowElement(execution.getActivityId());
            ActComment actComment=new ActComment();
            actComment.setMessage(comments);
            actComment.setUserId(userId);
            actComment.setStatus(status);
            actComment.setTaskId(taskId);
            actComment.setNodeName(currentFlowEl.getName());
            actComment.setNodeKey(currentFlowEl.getId());
            try {
                String comment=new ObjectMapper().writeValueAsString(actComment);
                Comment myComment = taskService.addComment(taskId,processInstanceId, comment);
            }catch (JsonProcessingException ex){
                throw new ActException(ex);
            }
            taskService.setVariable(taskId,ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS,status);
            if(isGroupUserTask){
                taskService.claim(task.getId(),userId);
            }

        }

        runtimeService.setVariable(processInstanceId,ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS,status);
        if(ActStatus.INSTANCE_APPROVED.equals(status) || ActStatus.START.equals(status)) {
            Object formObj= runtimeService.getVariable(processInstance.getId(),ActivitiUtils.INSTANCE_VAR_FORM);
            if(formObj!=null) {
                Map<String, Object> taskVar = new HashMap<>();
                taskVar.put("form", ActivitiUtils.convertJsonObjectToMap(formObj));
                taskService.complete(taskId, taskVar,true);
            }else{
                taskService.complete(taskId);
            }
        }else if(ActStatus.INSTANCE_DELETED.equals(status)){
            runtimeService.deleteProcessInstance(processInstanceId,comments);
        }else  if(ActStatus.INSTANCE_CANCELED.equals(status)){
            runtimeService.suspendProcessInstanceById(processInstanceId);
        }else if(ActStatus.INSTANCE_REJECTED.equals(status)){
            managementService.executeCommand(new ActStopCmd(taskId));
        }else {
            ActivitiUtils.setFailResult(result,"无效的审批状态");
            return result;
        }
        try{
            actApproveEventHandler.notice(ActivitiUtils.parseInstance(processInstance,null),task==null?null:ActivitiUtils.parseTask(task),status);
        }catch (Exception ex){
            logger.error("审批通知异常",ex);
        }

        ActivitiUtils.setOkResult(result);
        return result;
    }




    @Override
    public ActInstance create(String approvalCode, String instanceCode, String userId, String name, Map<String, Object> variables,Map<String,Object> form) {
        AssertUtils.isNotBlank(approvalCode,ActErrorCodeEnum.EMPTY_APPROVALCODE);
        AssertUtils.isNotBlank(instanceCode,ActErrorCodeEnum.EMPTY_INSTANCECODE);
        AssertUtils.isNotBlank(userId,ActErrorCodeEnum.EMPTY_USERID);
        AssertUtils.isNotBlank(name,ActErrorCodeEnum.EMPTY_INSTANCENAME);

        ActResult<String> result=new ActResult<>();
        long num= runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(instanceCode).processDefinitionKey(approvalCode).count();
        AssertUtils.isTrue(num==0, ActErrorCodeEnum.EXIST_INSTANCECODE);
        securityUtil.logInAs(userId);
//        Authentication.setAuthenticatedUserId(userId);
        ProcessInstance processInstance=runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionKey(approvalCode)
                .businessKey(instanceCode).variable(ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS,ActStatus.START).variable(ActivitiUtils.INSTANCE_VAR_FORM, form)
                .name(name)
                .start();
        AssertUtils.notNull(processInstance,ActErrorCodeEnum.FAIL_INSTANCESTART);
        return ActivitiUtils.parseInstance(processInstance,null);
    }

    @Override
    public PageDo<ActTask> activeTask(String userId, String groupId,int pageIndex,int pageSize,Map<String,Object> selected) {
        securityUtil.logInAs(userId);
        List<ActTask> myTaskList=new ArrayList<>();
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId).active();
        ProcessInstanceQuery instanceQuery=null;
        List<ProcessInstance> instanceList=null;
        if(selected!=null){
            String approvalCode=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_APPROVALCODE);
            String approvalName=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_APPROVALNAME);
            String instanceCode=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_INSTANCECODE);
            String instanceName=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_INSTANCENAME);
            String instanceUserId=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_INSTANCEUSERID);
            Date iStartTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_INSTANCETIME_START,ActivitiUtils.FORMAT_DATE);
            Date iEndTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_INSTANCETIME_END,ActivitiUtils.FORMAT_DATE);
            Date tStartTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_TASKTIME_START,ActivitiUtils.FORMAT_DATE);
            Date tEndTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_TASKTIME_END,ActivitiUtils.FORMAT_DATE);
            Date tOnTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_TASKTIME_ON,ActivitiUtils.FORMAT_DATE);

            if(tStartTime!=null){
                taskQuery.taskCreatedAfter(tStartTime);
            }
            if(tEndTime!=null){
                taskQuery.taskCreatedBefore(tEndTime);
            }
            if(tOnTime!=null){
                taskQuery.taskCreatedOn(tOnTime);
            }

            //处理instanceＱuery的过滤条件
            boolean hasInstanceName= org.apache.commons.lang3.StringUtils.isNotBlank(instanceName);
            boolean hasInstanceUserId= org.apache.commons.lang3.StringUtils.isNotBlank(instanceUserId);
            boolean hasInstanceStartTime=iStartTime!=null;
            boolean hasInstanceEndTime=iEndTime!=null;
            if(hasInstanceName || hasInstanceUserId || hasInstanceStartTime || hasInstanceEndTime){
                instanceQuery=runtimeService.createProcessInstanceQuery().active();
                if(hasInstanceName){
                    instanceQuery.processInstanceNameLike("%"+instanceName+"%");
                }
                if(hasInstanceUserId) {
                    instanceQuery.startedBy(instanceUserId);
                }
                if(hasInstanceStartTime) {
                    instanceQuery.startedAfter(iStartTime);
                }
                if(hasInstanceEndTime){
                    instanceQuery.startedBefore(iEndTime);
                }
            }
            if(org.apache.commons.lang3.StringUtils.isNotBlank(approvalCode)){
                taskQuery.processDefinitionKey(approvalCode);
                if(instanceQuery!=null){
                    instanceQuery.processDefinitionKey(approvalCode);
                }
            }
            if(org.apache.commons.lang3.StringUtils.isNotBlank(instanceCode)){
                taskQuery.processInstanceBusinessKey(instanceCode);
                if(instanceQuery!=null){
                    instanceQuery.processInstanceBusinessKey(instanceCode);
                }
            }
            if( org.apache.commons.lang3.StringUtils.isNotBlank(approvalName)){
                taskQuery.processDefinitionNameLike("%"+approvalName+"%");
            }

        }
        if(instanceQuery!=null){
            long instanceNum=instanceQuery.count();
            if(instanceNum==0){
//                PageData<ActTask> result=new PageData<>(myTaskList,0);
                PageDo<ActTask> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
                result.setTotalNum(0);
                return result;
            }
//            if(instanceNum<1000){
            instanceList=instanceQuery.list();
            List<String> instanceIdList= instanceList.stream().map(ProcessInstance::getId).collect(Collectors.toList());
            taskQuery.processInstanceIdIn(instanceIdList);
//            }

        }

        long count=taskQuery.count();
        if(count>0) {
            List<Task> tasks = taskQuery.orderByTaskCreateTime().desc().listPage((pageIndex - 1) * pageSize, pageSize);
            if(instanceList==null){
                Set<String> instanceIdList= tasks.stream().map(Task::getProcessInstanceId).collect(Collectors.toSet());
                instanceList=runtimeService.createProcessInstanceQuery().processInstanceIds(instanceIdList).list();
            }
            if(instanceList!=null){
                for(int i=0,k=tasks.size();i<k;i++){
                    Task task=tasks.get(i);
                    ProcessInstance instance= instanceList.stream().filter(t->t.getId().equals(task.getProcessInstanceId())).findFirst().get();
                    if(instance!=null) {
                        ActTask actTask = ActivitiUtils.parseTask(task);
                        actTask.setInstance(ActivitiUtils.parseInstance(instance, null));
                        myTaskList.add(actTask);
                    }
                }
            }

        }
        PageDo<ActTask> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        result.setTotalNum(count);
        result.setPage(myTaskList);
        return result;

    }

    @Override
    public PageDo<ActTask> finishedTask(String userId, String groupId,int pageIndex,int pageSize,Map<String,Object> selected) {
        PageDo<ActTask> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        securityUtil.logInAs(userId);
        HistoricTaskInstanceQuery taskInstanceQuery= historyService.createHistoricTaskInstanceQuery();
        taskInstanceQuery.taskAssignee(userId);
        HistoricProcessInstanceQuery instanceQuery=null;
        List<HistoricProcessInstance> instanceList=null;
//        instanceQuery.involvedUser(userId);
        List<ActTask> page = new ArrayList<>();
        if(selected!=null){
            String approvalCode=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_APPROVALCODE);
            String approvalName=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_APPROVALNAME);
            String instanceCode=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_INSTANCECODE);
            String instanceName=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_INSTANCENAME);
            String instanceUserId=ActivitiUtils.mapValueToString(selected,ActivitiUtils.KEY_INSTANCEUSERID);
            Date iStartTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_INSTANCETIME_START,ActivitiUtils.FORMAT_DATE);
            Date iEndTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_INSTANCETIME_END,ActivitiUtils.FORMAT_DATE);
            Date tStartTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_TASKTIME_START,ActivitiUtils.FORMAT_DATE);
            Date tEndTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_TASKTIME_END,ActivitiUtils.FORMAT_DATE);
            Date tOnTime=ActivitiUtils.mapValueToDate(selected,ActivitiUtils.KEY_TASKTIME_ON,ActivitiUtils.FORMAT_DATE);



            //处理instanceＱuery的过滤条件
            boolean hasInstanceName=StringUtils.isNotBlank(instanceName);
            boolean hasInstanceUserId=StringUtils.isNotBlank(instanceUserId);
            boolean hasInstanceStartTime=iStartTime!=null;
            boolean hasInstanceEndTime=iEndTime!=null;
            if(hasInstanceName || hasInstanceUserId || hasInstanceStartTime || hasInstanceEndTime){
                instanceQuery=historyService.createHistoricProcessInstanceQuery();
                if(hasInstanceName){
                    instanceQuery.processInstanceNameLike("%"+instanceName+"%");
                }
                if(hasInstanceUserId) {
                    instanceQuery.startedBy(instanceUserId);
                }
                if(hasInstanceStartTime) {
                    instanceQuery.startedAfter(iStartTime);
                }
                if(hasInstanceEndTime){
                    instanceQuery.startedBefore(iEndTime);
                }
            }

            if(StringUtils.isNotBlank(approvalCode)){
                taskInstanceQuery.processDefinitionKey(approvalCode);
                if(instanceQuery!=null){
                    instanceQuery.processDefinitionKey(approvalCode);
                }
            }
            if(StringUtils.isNotBlank(instanceCode)){
                taskInstanceQuery.processInstanceBusinessKey(instanceCode);
                if(instanceQuery!=null){
                    instanceQuery.processInstanceBusinessKey(instanceCode);
                }
            }
            if( StringUtils.isNotBlank(approvalName)){
                taskInstanceQuery.processDefinitionNameLike("%"+approvalName+"%");
            }

            if(instanceQuery!=null){
                long instanceNum=instanceQuery.count();
                if(instanceNum==0){
                    result.setTotalNum(0);
                    return result;
                }
                if(instanceNum<1000){
                    instanceList=instanceQuery.includeProcessVariables().list();
                    List<String> instanceIdList= instanceList.stream().map(HistoricProcessInstance::getId).collect(Collectors.toList());
                    taskInstanceQuery.processInstanceIdIn(instanceIdList);
                }

            }

        }

        taskInstanceQuery.orderBy(HistoricProcessInstanceQueryProperty.START_TIME).desc();
        long count=taskInstanceQuery.count();

        if(count>0) {
            List<HistoricTaskInstance> hisList = taskInstanceQuery.listPage((pageIndex - 1) * pageSize, pageSize);

            if(instanceList==null && hisList!=null && hisList.size()>0){
                Set<String> instanceIdList= hisList.stream().map(HistoricTaskInstance::getProcessInstanceId).collect(Collectors.toSet());
                instanceList=historyService.createHistoricProcessInstanceQuery().processInstanceIds(instanceIdList).includeProcessVariables().list();
            }
            if(instanceList!=null){
                for(int i=0,k=hisList.size();i<k;i++){
                    HistoricTaskInstance task=hisList.get(i);
                    HistoricProcessInstance instance= instanceList.stream().filter(t->t.getId().equals(task.getProcessInstanceId())).findFirst().get();
                    if(instance!=null) {
                        ActTask actTask = ActivitiUtils.parseTask(task);
                        ActInstance actInstance=ActivitiUtils.parseInstance(instance, null);
                        if(actInstance.getEndTime()==null && !ActStatus.INSTANCE_CANCELED.equals( actInstance.getStatus())){
                            actInstance.setStatus(ActStatus.INSTANCE_PENDING);
                        }else {

                        }
                        actInstance.setForm(null);
                        actTask.setInstance(actInstance);
                        page.add(actTask);
                    }
                }
            }

//            if (hisList != null) {
//                for (int i = 0, k = hisList.size(); i < k; i++) {
//                    HistoricTaskInstance task = hisList.get(i);
//                    String processInstanceId = task.getProcessInstanceId();
//                    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).includeProcessVariables().singleResult();
//                    if (processInstance == null) {
//                        continue;
//                    }
//                    ActTask actTask = ActivitiUtils.parseTask(task);
//                    ActInstance actInstance=new ActInstance();
//                    actTask.setInstance(ActivitiUtils.parseInstance(processInstance, actInstance));
//                    if(processInstance.getEndTime()==null && !ActStatus.INSTANCE_CANCELED.equals( actInstance.getStatus())){
//                        actInstance.setStatus(ActStatus.INSTANCE_PENDING);
//                    }
//                    page.add(actTask);
//                }
//            }
        }
        result.setTotalNum(count);
        result.setPage(page);
        return result;


    }

    @Override
    public InputStream image(String instanceCode) {
        // 根据业务ID，查询流程实例对象
        /*
         * 获取流程实例
         */
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(instanceCode).singleResult();
        if (processInstance == null) {
            return null;
        }
        // 根据流程对象获取流程对象模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        /*
         * 查看已执行的节点集合 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
         */
        // 构造历史流程查询
        HistoricActivityInstanceQuery historyInstanceQuery = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId());
        // 查询历史节点
        List<HistoricActivityInstance> historicActivityInstanceList = historyInstanceQuery.orderByHistoricActivityInstanceStartTime().asc().list();

        if (historicActivityInstanceList == null || historicActivityInstanceList.size() == 0) {
            return outputImg(bpmnModel, null, null);
        }
        //去掉正要处理的节点

        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).processInstanceBusinessKey(instanceCode).list();

        if (taskList != null) {
            for (int i = 0, k = taskList.size(); i < k; i++) {
                Task task = taskList.get(i);

                for(int delIndex=0,delSize=historicActivityInstanceList.size();delIndex<delSize;delIndex++){
                    HistoricActivityInstance hi_ins=historicActivityInstanceList.get(delIndex);
                    if(hi_ins.getActivityId().equals(task.getTaskDefinitionKey())){
                        historicActivityInstanceList.remove(delIndex);
                        break;
                    }
                }

            }
        }


        // 已执行的节点ID集合(将historicActivityInstanceList中元素的activityId字段取出封装到executedActivityIdList)
        List<String> executedActivityIdList = historicActivityInstanceList.stream().map(item -> item.getActivityId()).collect(Collectors.toList());

        /*
         * 获取流程走过的线
         */
        // 获取流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        List<String> flowIds = ActivitiUtils.getHighLightedFlows(bpmnModel, processDefinition, historicActivityInstanceList);

        return outputImg(bpmnModel, flowIds, executedActivityIdList);
    }

    /**
     * 输出图像
     * @param bpmnModel              图像对象
     * @param flowIds                已执行的线集合
     * @param executedActivityIdList void 已执行的节点ID集合
     */
    private InputStream outputImg(BpmnModel bpmnModel, List<String> flowIds, List<String> executedActivityIdList) {
        InputStream imageStream = null;
        try {
            imageStream = processDiagramGenerator.generateDiagram(bpmnModel, executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体", false, "png");

        } catch (Exception e) {
            logger.error("显示流程图跟踪报错, 错误信息:{}", e);
        }

        return imageStream;
    }

    @Override
    public PageDo<ActInstance> createInstanceFinishedPageList(int pageIndex, int pageSize,String createUserID, Map<String, Object> selected) {
        HistoricProcessInstanceQuery instanceQuery= historyService.createHistoricProcessInstanceQuery();
        instanceQuery.startedBy(createUserID);
        if(selected!=null){
            Object approvalCode=selected.get("approvalCode");
            Object instanceCode=selected.get("instanceCode");

            if(approvalCode!=null && !approvalCode.toString().isEmpty()){
                instanceQuery.processDefinitionKey(approvalCode.toString());
            }if(instanceCode!=null && !instanceCode.toString().isEmpty()){
                instanceQuery.processInstanceBusinessKey(instanceCode.toString());
            }
        }

        instanceQuery.orderBy(HistoricProcessInstanceQueryProperty.START_TIME).desc().includeProcessVariables().or().variableValueEquals(ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS,ActStatus.INSTANCE_CANCELED).finished().endOr();
        long count=instanceQuery.count();
        List<HistoricProcessInstance> hisList= instanceQuery.listPage((pageIndex-1)*pageSize,pageSize);
        PageDo<ActInstance> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        result.setTotalNum(count);
        List<ActInstance> page=new ArrayList<>();
        if(hisList!=null) {
            for (int i = 0, k = hisList.size(); i < k; i++) {
                HistoricProcessInstance instance = hisList.get(i);
                page.add(ActivitiUtils.parseInstance(instance, null));
            }
        }
        result.setPage(page);
        return result;
    }

    @Override
    public PageDo<ActInstance> createInstanceActivePageList(int pageIndex, int pageSize,String createUserId, Map<String, Object> selected) {
        ProcessInstanceQuery instanceQuery=runtimeService.createProcessInstanceQuery();
        instanceQuery.startedBy(createUserId);
        if(selected!=null){
            Object approvalCode=selected.get("approvalCode");
            Object instanceCode=selected.get("instanceCode");
            if(approvalCode!=null && !approvalCode.toString().isEmpty()){
                instanceQuery.processDefinitionKey(approvalCode.toString());
            }if(instanceCode!=null && !instanceCode.toString().isEmpty()){
                instanceQuery.processInstanceBusinessKey(instanceCode.toString());
            }
        }
        instanceQuery.active();
        long count=instanceQuery.count();
        List<ProcessInstance> processList=instanceQuery.orderBy(HistoricProcessInstanceQueryProperty.START_TIME).desc()
        .listPage((pageIndex-1)*pageSize,pageSize);
        PageDo<ActInstance> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        result.setTotalNum(count);
        List<ActInstance> page=new ArrayList<>();
        if(processList!=null) {
            for (int i = 0, k = processList.size(); i < k; i++) {
                ProcessInstance instance = processList.get(i);
                ActInstance actInstance=ActivitiUtils.parseInstance(instance, null);
                if(instance.isSuspended()){
                    actInstance.setStatus(ActStatus.INSTANCE_CANCELED);
                }else {
                    actInstance.setStatus(ActStatus.INSTANCE_PENDING);
                }
                page.add(actInstance);
            }
        }
        result.setPage(page);
        return result;
    }

    @Override
    public PageDo<ActInstance> createInstanceListPage(int pageIndex, int pageSize, String createUserId, Map<String, Object> selected) {
        HistoricProcessInstanceQuery instanceQuery= historyService.createHistoricProcessInstanceQuery();
        if(StringUtils.isNotBlank(createUserId)) {
            instanceQuery.startedBy(createUserId);
        }
        if(selected!=null){
            Object approvalCode=selected.get("approvalCode");
            Object instanceCode=selected.get("instanceCode");

            if(approvalCode!=null && !approvalCode.toString().isEmpty()){
                instanceQuery.processDefinitionKey(approvalCode.toString());
            }if(instanceCode!=null && !instanceCode.toString().isEmpty()){
                instanceQuery.processInstanceBusinessKey(instanceCode.toString());
            }
        }
        instanceQuery.orderBy(HistoricProcessInstanceQueryProperty.START_TIME).desc().includeProcessVariables();
        long count=instanceQuery.count();
        List<HistoricProcessInstance> hisList= instanceQuery.listPage((pageIndex-1)*pageSize,pageSize);
        PageDo<ActInstance> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        result.setTotalNum(count);
        List<ActInstance> page=new ArrayList<>();
        if(hisList!=null) {
            for (int i = 0, k = hisList.size(); i < k; i++) {
                HistoricProcessInstance instance = hisList.get(i);
                ActInstance actInstance=ActivitiUtils.parseInstance(instance, null);
                if(instance.getEndTime()==null && !ActStatus.INSTANCE_CANCELED.equals( actInstance.getStatus())){
                    actInstance.setStatus(ActStatus.INSTANCE_PENDING);
                }
                page.add(actInstance);
            }
        }
        result.setPage(page);
        return result;
    }

    @Override
    public List<ActProcessDefinition> processDefinitionList(){
        List<ActProcessDefinition> result=new ArrayList<>();
        List<ProcessDefinition> definitionList= repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().list();
        if(definitionList!=null){
            for(int i=0,k=definitionList.size();i<k;i++){
                ProcessDefinition p=definitionList.get(i);
                ActProcessDefinition myP=new ActProcessDefinition();
                myP.setCategory(p.getCategory());
                myP.setName(p.getName());
                myP.setKey(p.getKey());
                myP.setId(p.getId());
                result.add(myP);
            }
        }
        return result;
    }
}


