package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActApproval;

public interface ActExtendDataService {

    boolean insertActApproval(ActApproval actApproval);

    ActApproval findActApprovalByApprovalCode(String approvalCode);

}
