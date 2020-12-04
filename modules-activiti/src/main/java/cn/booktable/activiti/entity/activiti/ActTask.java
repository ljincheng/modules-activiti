package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ActTask {

    private String id;
    private String name;
    private String description;
    private int priority;
    private String owner;
    private String assignee;
    private String processInstanceId;
    private String executionId;
    private String processDefinitionId;
    private Date createTime;
    private Date dueDate;
    private String category;
    private String parentTaskId;
    private String tenantId;
    private String formKey;
    private Map<String,Object> taskLocalVariables;
    private Map<String,Object> processVariables;
    private Date claimTime;

    private ActInstance instance;
    private List<ActComment> comments;

    private String userName;
}
