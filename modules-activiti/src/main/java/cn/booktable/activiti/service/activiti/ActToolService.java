package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActTask;

import java.util.List;

public interface ActToolService {

    void matchInstanceUserName(ActInstance actInstance, ActUserService userService);

    void matchInstanceListUserName(List<ActInstance> actInstanceList, ActUserService userService);

    void matchActTaskListUserName(List<ActTask> tasks, ActUserService userService);

}
