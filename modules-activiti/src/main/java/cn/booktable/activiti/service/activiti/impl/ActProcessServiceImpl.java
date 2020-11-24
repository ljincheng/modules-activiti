package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.ActProcessBo;
import cn.booktable.activiti.entity.activiti.ActTask;
import cn.booktable.activiti.service.activiti.ActProcessService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.core.page.PageDo;
import com.alibaba.fastjson.JSON;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service("actProcessService")
public class ActProcessServiceImpl implements ActProcessService {
    private static Logger logger= LoggerFactory.getLogger(ActProcessServiceImpl.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    /** 流程图生成器 */
    @Autowired
    private ProcessDiagramGenerator processDiagramGenerator;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Override
    public boolean deploy(ActProcessBo processBo) {
        Deployment deploy = repositoryService.createDeployment()//创建一个部署的构建器
                .addClasspathResource(processBo.getBpmnFile())//从类路径中添加资源,一次只能添加一个资源
                .name(processBo.getName())//设置部署的名称
                .category(processBo.getCategory())//设置部署的类别
                .deploy();
        if(deploy!=null)
        {
            logger.info("deploy={}", JSON.toJSONString(deploy));
        }
        return false;
    }

    @Override
    public boolean deleteDeploy(String id) {
//        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
        if(pd==null){
            return false;
        }
        repositoryService.deleteDeployment(pd.getDeploymentId());
        return true;
    }

    @Override
    public List<ActProcessBo> processList(Map selected) {
        List<ActProcessBo> result=new ArrayList<>();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().latestVersion().orderByDeploymentId().desc().list();
        if(list!=null) {
            for (int i = 0; i < list.size(); i++) {
                ProcessDefinition p = list.get(i);
                ActProcessBo processBo = new ActProcessBo();
                processBo.setId(p.getId());
                processBo.setName(p.getName());
                processBo.setCategory(p.getCategory());
                processBo.setDeploymentId(p.getDeploymentId());
                processBo.setBpmnFile(p.getResourceName());
                processBo.setKey(p.getKey());
                result.add(processBo);
            }
        }
        return result;
    }

    @Override
    public PageDo<ActProcessBo> findDeployList(Long pageIndex, Integer pageSize, String processkey) {

        Long allCounts = repositoryService.createDeploymentQuery()// 创建部署对象查询
                .count();

        int counts = allCounts.intValue();


        int pageStart=(pageIndex==null?0:(pageIndex.intValue()-1)* pageSize.intValue());
        List<Deployment> list = repositoryService.createDeploymentQuery()// 创建部署对象查询
                .orderByDeploymenTime().desc()// 根据部署时间 降序
                // .list(); //全部查询
                .listPage(pageStart, pageSize); // 分页查询

        List<ActProcessBo> proList=new ArrayList<>();

        if(list!=null){
            for(int i=0,k=list.size();i<k;i++){
                Deployment deployment=list.get(i);
                ActProcessBo p=new ActProcessBo();
                p.setKey(deployment.getKey());
                p.setId(deployment.getId());
                p.setCategory(deployment.getCategory());
                p.setDeploymentId(deployment.getTenantId());
                p.setName(deployment.getName());
                p.setDeploymentTime(deployment.getDeploymentTime());
                proList.add(p);
            }
        }
        PageDo<ActProcessBo> lstResult = new PageDo<>(pageIndex,pageSize);
        lstResult.setTotalNum(counts);
        lstResult.setTotalPage(counts/pageSize);
        lstResult.setPage(proList);
        return lstResult;

    }

    @Override
    public InputStream findProcessImage(String id) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
//        String diagramResourceName = procDef.getDiagramResourceName();
        logger.info("流程图片:{}", pd.getDiagramResourceName());
        return repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
    }



    @Override
    public InputStream findImageProcess(String processkey) {
        // 根据业务ID，查询流程实例对象
        /*
         * 获取流程实例
         */
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(processkey).singleResult();
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
            imageStream = processDiagramGenerator.generateDiagram(bpmnModel, executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体", true, "png");
        } catch (Exception e) {
            logger.error("显示流程图跟踪报错, 错误信息:{}", e);
        }

        return imageStream;
    }


    @Override
    public void startProcessInstanceByKey(String processKey, Map<String, Object> variables, String businessKey) {
        runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
    }

    @Override
    public void approveInstance(String taskId, String businessKey, String comments,String userId, Map<String, Object> variables) {
        // 使用任务ID，查询任务对象，获取流程流程实例ID
        Task task = taskService.createTaskQuery()//
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
    public List<ActTask> instanceList(String businessKey) {
        List<ActTask> result=new ArrayList<>();

        // 查询流程实例
        List<ProcessInstance> lstPis = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
        if(lstPis!=null && lstPis.size()>0) {
            // 获取流程实例的ID，这里确切的说只有一条数据，保险起见，弄个Set存放一下
            Set<String> st = new HashSet<String>();
            for (ProcessInstance pi : lstPis) {
                String piId = pi.getProcessInstanceId();
                st.add(piId);
            }
            List<String> ids = new ArrayList<>(st);
            List<Task> lst = taskService.createTaskQuery().processInstanceIdIn(ids).list();
            if(lst!=null && lst.size()>0)
            {
                for(int i=0,k=lst.size();i<k;i++){
                    Task task=lst.get(i);
                    ActTask actTask=new ActTask();
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
                    result.add(actTask);
                }
            }

        }
        return result;
    }

    @Override
    public List<ActTask> instanceList(String businessKey, String userName) {
        List<ActTask> result=new ArrayList<>();

        // 查询流程实例
        List<ProcessInstance> lstPis = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
        if(lstPis!=null && lstPis.size()>0) {
            // 获取流程实例的ID，这里确切的说只有一条数据，保险起见，弄个Set存放一下
            Set<String> st = new HashSet<String>();
            for (ProcessInstance pi : lstPis) {
                String piId = pi.getProcessInstanceId();
                st.add(piId);
            }
            for (String piId : st) {
                List<Task> lst = taskService.createTaskQuery().processInstanceId(piId).taskAssignee(userName).list();
                if(lst!=null && lst.size()>0)
                {
                    for(int i=0,k=lst.size();i<k;i++){
                        Task task=lst.get(i);
                        ActTask actTask=new ActTask();
                        actTask.setId(task.getId());
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
                        result.add(actTask);
                    }
                }
            }


        }
        return result;
    }
}
