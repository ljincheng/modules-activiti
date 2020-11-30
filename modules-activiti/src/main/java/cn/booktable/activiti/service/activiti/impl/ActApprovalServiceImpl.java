package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.ActApproval;
import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.service.activiti.ActApprovalService;
import cn.booktable.activiti.service.activiti.ActExtendDataService;
import cn.booktable.activiti.service.activiti.ActModelService;
import cn.booktable.activiti.utils.ActivitiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("actApprovalService")
public class ActApprovalServiceImpl implements ActApprovalService {

    @Override
    public ActResult<Void> approve(String taskId, String instanceCode, String status, String comments, String userId, Map<String, Object> variables) {
        return null;
    }


}
