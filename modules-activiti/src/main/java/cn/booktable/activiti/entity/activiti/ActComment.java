package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.Date;

@Data
public class ActComment {
    private  String userId;
    private String id;
    private String comment;
    private Date  createTime;
    private String taskId;
    private String status;
    private String message;

}
