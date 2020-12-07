package cn.booktable.activiti.entity.activiti;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class ActProcessDefinition {
    private String id;
    private String category;
    private String name;
    private String key;
    private String desciption;
    private Integer version;
    private String resourceName;
    private String deploymentId;


}
