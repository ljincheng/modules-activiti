package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActApproval;
import cn.booktable.activiti.entity.activiti.ActResult;

/**
 * 审批定义
 */
public interface ActApprovalService {


    /**
     * 创建审批定义
     * @param approvalCode
     * @param approvalName
     * @param category
     * @param description
     * @return
     */
    ActResult<ActApproval> create(String approvalCode,String approvalName,String category,String description);


    ActResult<String> updateModel(ActApproval approval,String modelJson,String modelSvg);

    /**
     * 部署审批定义
     * @param approvalCode
     * @return
     */
    ActResult<String> deploy(String approvalCode);
}
