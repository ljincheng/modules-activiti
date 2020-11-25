package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActResult;

import java.util.List;
import java.util.Map;

public interface ActInstanceService {



    List<ActInstance> queryListAll(String approvalCode,String deploymentId);

    ActInstance detail(String instanceCode);

    /**
     * 审批
     * @param taskId
     * @param instanceCode
     * @param comments
     * @param userId
     * @param variables
     */
    void approve(String taskId, String instanceCode, String comments,String userId, Map<String, Object> variables);

    /**
     *创建审批实例
     * @param approvalCode
     * @param instanceCode
     * @param userId
     * @param variables
     */
    ActResult<String> create(String approvalCode, String instanceCode, String userId, String name, Map<String, Object> variables);
}
