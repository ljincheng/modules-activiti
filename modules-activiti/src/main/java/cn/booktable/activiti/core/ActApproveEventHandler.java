package cn.booktable.activiti.core;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActTask;

/**
 * 审批通知事件
 */
public interface ActApproveEventHandler {

    public void notice(ActInstance instance, ActTask task, String status);

}
