package cn.booktable.activiti.entity.activiti;


import lombok.Data;

import java.util.Date;


@Data
public class ActProcessBo {

    private String id;
    private String category;
    private String name;
    private String bpmnFile;
    private String key;
    private String deploymentId;
    private Date deploymentTime;

}
