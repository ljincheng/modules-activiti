package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.*;
import cn.booktable.activiti.service.activiti.ActInstanceMetaInfoHelper;
import cn.booktable.activiti.service.activiti.ActInstanceService;
import cn.booktable.activiti.utils.ActivitiUtils;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        }else {
             HistoricProcessInstance historicProcessInstance= historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(instanceCode).singleResult();
             if(historicProcessInstance!=null){
                 processDefinitionId=historicProcessInstance.getProcessDefinitionId();
                 ActivitiUtils.parseInstance(historicProcessInstance,actInstance);
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

        List<ActTimeline> timelines=new ArrayList<>();
        actInstance.setTimelineList(timelines);
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        if(model != null) {
            Collection<FlowElement>  flowElements = model.getMainProcess().getFlowElements();
            for(FlowElement e : flowElements) {
                ActTimeline timeline=getActTimelineFromFlow(e);
                if(timeline!=null){
                    timelines.add(timeline);
                }
            }
        }

        return actInstance;
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
        System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
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
        TaskQuery taskQuery=taskService.createTaskQuery().processInstanceBusinessKey(instanceCode)
                .taskId(taskId);
        long num=taskQuery.count();
        if(num<1){
            ActivitiUtils.setFailResult(result,"无效参数");
            return result;
        }
        // 使用任务ID，查询任务对象，获取流程流程实例ID
        Task task =taskQuery.taskId(taskId).singleResult();
//        String taskUserId=task.getAssignee();
//        if(taskUserId!=null && taskUserId.length()>0){//单个审批人
//            if(!ActStatus.START.equals(status) && !taskUserId.equals(userId)){
//                ActivitiUtils.setFailResult(result,"审批人无权限操作当前流程");
//                return result;
//            }
//        }else {
//            if(!ActStatus.START.equals(status)) {
//                ActivitiUtils.setFailResult(result, "当前流程节点没有设置审批人");
//                return result;
//            }
//        }

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

        if(ActStatus.APPROVED.equals(status) || ActStatus.START.equals(status)) {
            taskService.complete(taskId, ActInstanceMetaInfoHelper.createInstanceVariables(userId, null, ActStatus.APPROVED, variables), true);
        }else if(ActStatus.REJECTED.equals(status)){
            //taskService.complete(taskId, ActInstanceMetaInfoHelper.createInstanceVariables(userId, null, ActStatus.APPROVED, variables), true);
            managementService.executeCommand(new ActEndCmd(taskId));
//            Task nextTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
//            if(nextTask!=null) {
//
//            }
        }

        /**
         * 在完成任务之后，判断流程是否结束 如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
         */


        return result;
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

        Map<String ,Object> instanceVariables= ActInstanceMetaInfoHelper.createInstanceVariables(userId,null,ActStatus.START,variables);
//        ProcessInstance processInstance= runtimeService.startProcessInstanceByKey(approvalCode, instanceCode, instanceVariables);
        ProcessInstance processInstance=runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionKey(approvalCode)
                .businessKey(instanceCode)
                .variables(instanceVariables)
                .name(name)
                .start();
        if(processInstance!=null) {
//            List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).processInstanceBusinessKey(instanceCode).list();
//            if(taskList!=null && taskList.size()==1) {
//                approve(taskList.get(0).getId(), instanceCode,ActStatus.START, "提交", userId, variables);
//            }
            ActivitiUtils.setOkResult(result);
        }else {
            ActivitiUtils.setFailResult(result,"发起实例失败");
        }
        return result;
    }

    @Override
    public List<ActTask> activeTask(String userId, String groupId) {
        List<ActTask> myTaskList=new ArrayList<>();
        Authentication.setAuthenticatedUserId(userId);
//        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(userId);
        List<Task> tasks = taskQuery.orderByTaskCreateTime().desc().list();
        for (Task task : tasks) {
            String processInstanceId = task.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
            if (processInstance == null) {
                continue;
            }
            ActTask actTask=ActivitiUtils.parseTask(task);
            actTask.setInstance(ActivitiUtils.parseInstance(processInstance,null));
            myTaskList.add(actTask);

        }
        return myTaskList;
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
}


