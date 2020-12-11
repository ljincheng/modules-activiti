package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActProcessDefinition;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.entity.activiti.ActTask;
import cn.booktable.core.page.PageDo;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ActInstanceService {



    List<ActInstance> queryListAll(String approvalCode,String deploymentId);

    ActInstance detail(String instanceCode);

    /**
     * 审批
     * @param taskId
     * @param instanceCode
     * @param status
     * @param comments
     * @param userId
     * @param variables
     */
    ActResult<Void> approve(String taskId, String instanceCode,String status, String comments,String userId, Map<String, Object> variables);


    /**
     *创建审批实例
     * @param approvalCode
     * @param instanceCode
     * @param userId
     * @param variables
     */
    ActInstance create(String approvalCode, String instanceCode, String userId, String name, Map<String, Object> variables,Map<String,Object> form);


    /**
     * 获取用户待办任务
     * @param userId
     * @param groupId
     * @return
     */
    PageDo<ActTask> activeTask(String userId,String groupId,int pageIndex,int pageSize,Map<String,Object> selected);

    /**
     * 获取已办任务
     * @param userId
     * @param groupId
     * @return
     */
    PageDo<ActTask> finishedTask(String userId,String groupId,int pageIndex,int pageSize,Map<String,Object> selected);


    public InputStream image(String instanceCode);


    /**
     * 创建人申请审批已完成流程
     * @param pageIndex
     * @param pageSize
     * @param createUserId 申请流程创建人
     * @param selected
     * @return
     */
    PageDo<ActInstance> createInstanceFinishedPageList(int pageIndex,int pageSize,String createUserId,Map<String,Object> selected);

    /**
     * 创建人申请审批中的流程
     * @param pageIndex
     * @param pageSize
     * @param createUserId 申请流程创建人
     * @param selected
     * @return
     */
    PageDo<ActInstance> createInstanceActivePageList(int pageIndex,int pageSize,String createUserId,Map<String,Object> selected);


    /**
     * 我的申请流程列表
     * @param pageIndex
     * @param pageSize
     * @param createUserId
     * @param selected
     * @return
     */
    PageDo<ActInstance> createInstanceListPage(int pageIndex,int pageSize,String createUserId,Map<String,Object> selected);

    List<ActProcessDefinition> processDefinitionList();
}
