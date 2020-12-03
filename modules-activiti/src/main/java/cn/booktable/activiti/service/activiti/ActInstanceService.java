package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
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
    ActResult<String> create(String approvalCode, String instanceCode, String userId, String name, Map<String, Object> variables,Map<String,Object> form);


    /**
     * 获取用户待办任务
     * @param userId
     * @param groupId
     * @return
     */
    List<ActTask> activeTask(String userId,String groupId);


    public InputStream image(String instanceCode);


    /**
     * 历史审批流程
     * @param pageIndex
     * @param pageSize
     * @param selected
     * @return
     */
    PageDo<ActInstance> historyPageList(int pageIndex,int pageSize,Map<String,Object> selected);

    /**
     * 审批中的流程
     * @param pageIndex
     * @param pageSize
     * @param selected
     * @return
     */
    PageDo<ActInstance> processPageList(int pageIndex,int pageSize,Map<String,Object> selected);
}
