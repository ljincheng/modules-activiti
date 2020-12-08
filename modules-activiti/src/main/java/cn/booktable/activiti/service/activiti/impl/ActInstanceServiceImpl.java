package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.core.*;
import cn.booktable.activiti.entity.activiti.*;
import cn.booktable.activiti.service.activiti.ActInstanceService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.activiti.utils.AssertUtils;
import cn.booktable.core.page.PageDo;
import cn.booktable.util.StringUtils;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.impl.RepositoryServiceImpl;
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
            List<HistoricVariableInstance> formList=historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).variableName("form").list();
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
            for(int i=0,k=commentList.size();i<k;i++){
                Comment comment=commentList.get(i);
                ActComment actComment=new ActComment();
                actComment.setComment(comment.getFullMessage());
                actComment.setId(comment.getId());
                actComment.setUserId(comment.getUserId());
                actComment.setCreateTime(comment.getTime());
                actComment.setTaskId(comment.getTaskId());
                actComment.setStatus(ActivitiUtils.calculateCommentStatus(comment));
                String fullMessage=comment.getFullMessage();
                if(fullMessage.startsWith("[") && fullMessage.indexOf("]")>0){
                    int endStatusIndex=fullMessage.indexOf("]");
                    String status=fullMessage.substring(1,endStatusIndex);
                    actComment.setStatus(status);
                    actComment.setMessage(fullMessage.substring(endStatusIndex+1));
                }else {
                    actComment.setMessage(fullMessage);
                }
                actCommentList.add(actComment);
            }

        }

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
        AssertUtils.isNotBlank(taskId, ActErrorCodeEnum.EMPTY_TASKID);
        AssertUtils.isNotBlank(instanceCode, ActErrorCodeEnum.EMPTY_INSTANCECODE);
        AssertUtils.isNotBlank(status, ActErrorCodeEnum.EMPTY_APPROVALSTATUS);
        AssertUtils.hasIn(status, ActErrorCodeEnum.EXCLUDE_APPROVALSTATUS,ActStatus.INSTANCE_APPROVED,ActStatus.INSTANCE_REJECTED,ActStatus.INSTANCE_CANCELED,ActStatus.INSTANCE_DELETED);

        TaskQuery taskQuery=taskService.createTaskQuery().processInstanceBusinessKey(instanceCode).active().taskId(taskId);
        long num=taskQuery.count();
        AssertUtils.isTrue(num>0, ActErrorCodeEnum.UNEXIST_ACTIVETASK);


        // 使用任务ID，查询任务对象，获取流程流程实例ID
        securityUtil.logInAs(userId);
        Task task =taskQuery.singleResult();
        boolean isGroupUserTask=false;
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

        // 获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        boolean suspended = processInstance.isSuspended();
        AssertUtils.isTrue(!suspended,ActErrorCodeEnum.INSTANCE_SUSPENDED);
        if(isGroupUserTask){
            taskService.claim(task.getId(),userId);
        }
        // 使用任务ID，完成当前人的个人任务，同时流程变量
        String taskComment="["+status+"]";
        if(comments!=null){
            taskComment=taskComment+comments;
        }
        Comment myComment=  taskService.addComment(taskId, processInstanceId, taskComment);
        taskService.setVariable(taskId,ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS,status);
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
            actApproveEventHandler.notice(ActivitiUtils.parseInstance(processInstance,null),ActivitiUtils.parseTask(task),status);
        }catch (Exception ex){
            logger.error("审批通知异常",ex);
        }
        ActivitiUtils.setOkResult(result);
        return result;
    }




    @Override
    public ActResult<String> create(String approvalCode, String instanceCode, String userId, String name, Map<String, Object> variables,Map<String,Object> form) {
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
        if(processInstance!=null) {
            ActivitiUtils.setOkResult(result);
        }else {
            ActivitiUtils.setFailResult(result,"发起实例失败");
        }
        return result;
    }

    @Override
    public PageDo<ActTask> activeTask(String userId, String groupId,int pageIndex,int pageSize,Map<String,Object> selected) {
        securityUtil.logInAs(userId);
        List<ActTask> myTaskList=new ArrayList<>();
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId).active();
        if(selected!=null){
            Object approvalCode=selected.get("approvalCode");
            Object instanceCode=selected.get("instanceCode");
            if(approvalCode!=null && !approvalCode.toString().isEmpty()){
                taskQuery.processDefinitionKey(approvalCode.toString());
            }if(instanceCode!=null && !instanceCode.toString().isEmpty()){
                taskQuery.processInstanceBusinessKey(instanceCode.toString());
            }
        }
        long count=taskQuery.count();
        PageDo<ActTask> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        result.setTotalNum(count);
        if(count>0) {
            List<Task> tasks = taskQuery.orderByTaskCreateTime().desc().listPage((pageIndex - 1) * pageSize, pageSize);
            for (Task task : tasks) {
                String processInstanceId = task.getProcessInstanceId();
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
                if (processInstance == null) {
                    continue;
                }
                ActTask actTask = ActivitiUtils.parseTask(task);
                actTask.setInstance(ActivitiUtils.parseInstance(processInstance, null));
                myTaskList.add(actTask);

            }
            result.setPage(myTaskList);
        }
        return result;
    }

    @Override
    public PageDo<ActTask> finishedTask(String userId, String groupId,int pageIndex,int pageSize,Map<String,Object> selected) {
        PageDo<ActTask> result=new PageDo<>(Long.valueOf(pageIndex),pageSize);
        securityUtil.logInAs(userId);
        HistoricTaskInstanceQuery instanceQuery= historyService.createHistoricTaskInstanceQuery();
        instanceQuery.taskAssignee(userId);
//        HistoricProcessInstanceQuery instanceQuery= historyService.createHistoricProcessInstanceQuery();
//        instanceQuery.involvedUser(userId);
        if(selected!=null){
            Object approvalCode=selected.get("approvalCode");
            Object instanceCode=selected.get("instanceCode");
            if(approvalCode!=null && !approvalCode.toString().isEmpty()){
                instanceQuery.processDefinitionKey(approvalCode.toString());
            }if(instanceCode!=null && !instanceCode.toString().isEmpty()){
                instanceQuery.processInstanceBusinessKey(instanceCode.toString());
            }
        }

        instanceQuery.orderBy(HistoricProcessInstanceQueryProperty.START_TIME).desc().finished();
        long count=instanceQuery.count();
        result.setTotalNum(count);
        if(count>0) {
            List<HistoricTaskInstance> hisList = instanceQuery.listPage((pageIndex - 1) * pageSize, pageSize);
            List<ActTask> page = new ArrayList<>();
            if (hisList != null) {
                for (int i = 0, k = hisList.size(); i < k; i++) {
                    HistoricTaskInstance task = hisList.get(i);
                    String processInstanceId = task.getProcessInstanceId();
                    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).includeProcessVariables().singleResult();
                    if (processInstance == null) {
                        continue;
                    }
                    ActTask actTask = ActivitiUtils.parseTask(task);
                    ActInstance actInstance=new ActInstance();
                    actTask.setInstance(ActivitiUtils.parseInstance(processInstance, actInstance));
                    if(processInstance.getEndTime()==null && !ActStatus.INSTANCE_CANCELED.equals( actInstance.getStatus())){
                        actInstance.setStatus(ActStatus.INSTANCE_PENDING);
                    }
                    page.add(actTask);
                }
            }
            result.setPage(page);
        }
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


