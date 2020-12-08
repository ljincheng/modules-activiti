package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.core.ActApproveEventHandler;
import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;


public class ActApproveEventHandlerImpl implements ActApproveEventHandler {

    @Override
    public void notice(ActInstance instance, ActTask task, String status) {

        System.out.println("====================ActApproveEventHandler: "+status);
    }
}
