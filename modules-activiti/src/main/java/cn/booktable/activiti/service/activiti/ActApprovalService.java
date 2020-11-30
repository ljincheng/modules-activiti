package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActApproval;
import cn.booktable.activiti.entity.activiti.ActResult;

import java.util.Map;

/**
 * 审批定义
 */
public interface ActApprovalService {

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
}
