package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.Map;

@Data
public class ActInstanceMetaInfo {
    private String name;
    private String userId;
    private String userName;
    private Map<String,Object> form;
    private String status;
}
