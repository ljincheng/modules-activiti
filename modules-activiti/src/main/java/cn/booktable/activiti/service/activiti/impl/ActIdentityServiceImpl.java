package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.service.activiti.ActIdentityService;
import org.springframework.stereotype.Service;

@Service("actIdentityService")
public class ActIdentityServiceImpl implements ActIdentityService {

    @Override
    public String assignee(String employee) {
        System.out.println("=============== Assignee:"+employee);
        return "114";
    }
}
