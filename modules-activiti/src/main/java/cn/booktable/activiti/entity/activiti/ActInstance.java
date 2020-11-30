package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ActInstance {
    private String id;
    private String approvalCode;//审批定义Code
    private String approvalName;//名称
    private String instanceName;
    private String instanceCode;
    private Date   startTime;
    private String deploymentId;
    private String userId;
    private String status;
    private String description;

    private List<ActTask> taskList;
    private List<ActComment> commentList;
    private List<ActTimeline> timelineList;

}
