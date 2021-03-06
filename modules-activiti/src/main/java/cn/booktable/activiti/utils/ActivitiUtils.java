package cn.booktable.activiti.utils;

import cn.booktable.activiti.core.ActErrorCodeEnum;
import cn.booktable.activiti.core.ActException;
import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.core.ActStatus;
import cn.booktable.activiti.entity.activiti.ActTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ActivitiUtils {

    public static String INSTANCE_VAR_APPROVALSTATUS="ApprovalStatus";
    public static String INSTANCE_VAR_FORM="form";
    public static String KEY_APPROVALCODE="approvalCode";
    public static String KEY_APPROVALNAME="approvalName";
    public static String KEY_INSTANCECODE="instanceCode";
    public static String KEY_INSTANCENAME="instanceName";
    public static String KEY_INSTANCEUSERID="instanceUserId";
    public static String KEY_INSTANCETIME_START="iStartTime";
    public static String KEY_INSTANCETIME_END="iEndTime";
    public static String KEY_TASKTIME_START="tStartTime";
    public static String KEY_TASKTIME_END="tEndTime";
    public static String KEY_TASKTIME_ON="tOnTime";
    public static String FORMAT_DATE="yyyy-MM-dd";

    /**
     * 获取流程走过的线
     * @param bpmnModel                 流程对象模型
     * @param processDefinitionEntity   流程定义对象
     * @param historicActivityInstances 历史流程已经执行的节点，并已经按执行的先后顺序排序
     * @return List<String> 流程走过的线
     */
    public static List<String> getHighLightedFlows(BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        // 用以保存高亮的线flowId
        List<String> highFlows = new ArrayList<String>();
        if (historicActivityInstances == null || historicActivityInstances.size() == 0)
            return highFlows;

        // 遍历历史节点
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 取出已执行的节点
            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);

            // 用以保存后续开始时间相同的节点
            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();

            // 获取下一个节点（用于连线）
            FlowNode sameActivityImpl = getNextFlowNode(bpmnModel, historicActivityInstances, i, activityImpl_);
//          FlowNode sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i + 1).getActivityId());

            // 将后面第一个节点放在时间相同节点的集合里
            if (sameActivityImpl != null)
                sameStartTimeNodes.add(sameActivityImpl);

            // 循环后面节点，看是否有与此后继节点开始时间相同的节点，有则添加到后继节点集合
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().getTime() != activityImpl2.getStartTime().getTime())
                    break;

                // 如果第一个节点和第二个节点开始时间相同保存
                FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                sameStartTimeNodes.add(sameActivityImpl2);
            }

            // 得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());
            // 取出节点的所有出去的线，对所有的线进行遍历
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();
            for (SequenceFlow pvmTransition : pvmTransitions) {
                // 获取节点
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());

                // 不是后继节点
                if (!sameStartTimeNodes.contains(pvmActivityImpl))
                    continue;

                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                highFlows.add(pvmTransition.getId());
            }
        }

        // 返回高亮的线
        return highFlows;
    }

    /**
     * 获取下一个节点信息
     * @param bpmnModel                 流程模型
     * @param historicActivityInstances 历史节点
     * @param i                         当前已经遍历到的历史节点索引（找下一个节点从此节点后）
     * @param activityImpl_             当前遍历到的历史节点实例
     * @return FlowNode 下一个节点信息
     */
    private static FlowNode getNextFlowNode(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances, int i, HistoricActivityInstance activityImpl_) {
        // 保存后一个节点
        FlowNode sameActivityImpl = null;

        // 如果当前节点不是用户任务节点，则取排序的下一个节点为后续节点
        if (!"userTask".equals(activityImpl_.getActivityType())) {
            // 是最后一个节点，没有下一个节点
            if (i == historicActivityInstances.size())
                return sameActivityImpl;
            // 不是最后一个节点，取下一个节点为后继节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i + 1).getActivityId());// 找到紧跟在后面的一个节点
            // 返回
            return sameActivityImpl;
        }

        // 遍历后续节点，获取当前节点后续节点
        for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
            // 后续节点
            HistoricActivityInstance activityImp2_ = historicActivityInstances.get(k);
            // 都是userTask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
            if ("userTask".equals(activityImp2_.getActivityType()) && activityImpl_.getStartTime().getTime() == activityImp2_.getStartTime().getTime())
                continue;
            // 找到紧跟在后面的一个节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());
            break;
        }
        return sameActivityImpl;
    }

    public static boolean isOkResult(ActResult result){
        if(result==null || result.getCode()==null || result.getCode().intValue()!=1)
        {
            return false;
        }
        return true;
    }

    public static boolean isFailResult(ActResult result){
        return !isOkResult(result);
    }


    public static void setFailResult(ActResult result,Exception ex){
        result.setCode(0);
        result.setMsg(ex.getMessage());
    }

    public static void setFailResult(ActResult result,String msg){
        result.setCode(0);
        result.setMsg(msg);
    }

    public static void setOkResult(ActResult result){
        result.setCode(1);
        result.setMsg("ok");
    }



    public static ActInstance parseInstance(ProcessInstance instance,ActInstance actInstance) {
        if (instance == null) {
            return null;
        }
        if (actInstance == null){
             actInstance = new ActInstance();
         }
        actInstance.setApprovalCode(instance.getProcessDefinitionKey());
        actInstance.setInstanceCode(instance.getBusinessKey());
        actInstance.setApprovalName(instance.getProcessDefinitionName());
        actInstance.setInstanceName(instance.getName());
        actInstance.setStartTime(instance.getStartTime());
        actInstance.setDeploymentId(instance.getDeploymentId());
        actInstance.setId(instance.getId());
        actInstance.setUserId(instance.getStartUserId());
        actInstance.setDescription(instance.getDescription());
        actInstance.setVariables(instance.getProcessVariables());
        Map<String,Object> instanceVar= actInstance.getVariables();
        if(instanceVar!=null ){
            Object approvalStatus=instanceVar.get(ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS);
            if(approvalStatus!=null){
                actInstance.setStatus(approvalStatus.toString());
            }
        }
        return actInstance;
    }


    public static ActInstance parseInstance(HistoricProcessInstance instance, ActInstance actInstance) {
        if (instance == null) {
            return null;
        }
        if (actInstance == null){
             actInstance = new ActInstance();
         }
        actInstance.setApprovalCode(instance.getProcessDefinitionKey());
        actInstance.setInstanceCode(instance.getBusinessKey());
        actInstance.setApprovalName(instance.getProcessDefinitionName());
        actInstance.setInstanceName(instance.getName());
        actInstance.setStartTime(instance.getStartTime());
        actInstance.setEndTime(instance.getEndTime());
        actInstance.setDeploymentId(instance.getDeploymentId());
        actInstance.setId(instance.getId());
        actInstance.setUserId(instance.getStartUserId());
        actInstance.setDescription(instance.getDescription());
        actInstance.setVariables(instance.getProcessVariables());
        Map<String,Object> instanceVar= actInstance.getVariables();
        if(instanceVar!=null ){
            Object approvalStatus=instanceVar.get(ActivitiUtils.INSTANCE_VAR_APPROVALSTATUS);
            if(approvalStatus!=null){
                actInstance.setStatus(approvalStatus.toString());
            }
        }
        return actInstance;
    }

    public static String calculateInstanceStatus(ProcessInstance instance){
        if (instance.isSuspended()) {
//            return org.activiti.api.process.model.ProcessInstance.ProcessInstanceStatusbing.SUSPENDED;
            return ActStatus.INSTANCE_CANCELED;
        } else if (instance.isEnded()) {
//            return org.activiti.api.process.model.ProcessInstance.ProcessInstanceStatus.COMPLETED;
            return ActStatus.INSTANCE_APPROVED ;//|| ActStatus.INSTANCE_REJECTED || ActStatus.INSTANCE_DELETED;
        }
        return ActStatus.INSTANCE_PENDING;
    }

    public static String calculateCommentStatus(Comment comment){
        if(comment!=null)
        {
            String msg=comment.getFullMessage();
            if(msg.startsWith("[") && msg.indexOf("]")>0){
                String status=msg.substring(1,msg.indexOf("]"));
                return status;
            }
        }
        return null;
    }


    public static ActTask parseTask(Task task){
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
        return actTask;
    }

    public static ActTask parseTask(HistoricTaskInstance task){
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
        actTask.setEndTime(task.getEndTime());
        actTask.setStartTime(task.getStartTime());
        return actTask;
    }

    public static Map<String,Object> convertJsonObjectToMap(Object formObj){
        if(formObj!=null ){
           if (formObj instanceof Map) {
                return (Map)formObj;
            }else {
               ObjectMapper mapper = new ObjectMapper();
                return mapper.convertValue(formObj,Map.class);
           }
        }
        return null;
    }

    public static String approvalStatusToName(String status)
    {
        String approvalType=status;
        if(status!=null && status.length()>0) {
            if (ActStatus.INSTANCE_PENDING.equals(status)) {
                approvalType = "审批中";
            } else if (ActStatus.INSTANCE_APPROVED.equals(status) ) {
                approvalType = "通过";
            } else if (ActStatus.INSTANCE_REJECTED.equals(status)) {
                approvalType = "拒绝";
            } else if (ActStatus.INSTANCE_CANCELED.equals(status)) {
                approvalType = "撤回";
            } else if (ActStatus.INSTANCE_DELETED.equals(status)) {
                approvalType = "删除";
            } else if (ActStatus.INSTANCE_DONE.equals(status)) {
                approvalType = "已完成";
            }else if (ActStatus.START.equals(status)) {
                approvalType = "已提交";
            }
        }
        return approvalType;
    }


    public static String mapValueToString(Map<String,Object> map,String key){
        Object value=map.get(key);
        if(value==null){
            return null;
        }
        if(value instanceof String){
            return (String)value;
        }
        return value.toString();
    }

    public static Date mapValueToDate(Map<String,Object> map, String key, String pattern){
        Object value=map.get(key);
        if(value==null){
            return null;
        }
        if(value instanceof Date){
            return (Date)value;
        }

        if(value.toString().length()>0) {
            try {
                return new SimpleDateFormat(pattern).parse(value.toString());
            }catch (ParseException ex){
                throw new ActException(ActErrorCodeEnum.ERROR_FORMAT_DATE.msg());
            }
        }
        return null;
    }
}
