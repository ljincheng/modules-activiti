package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.Date;

@Data
public class ActModel {
    private String id;
    private String name;
    private String category;
    private String key;
    private String description;
    private Integer version;
    private String json;
    private String svg;
    private String metaInfo;
    private String deploymentId;
    private String tenantId;
    private Date createTime;
    private Date lastUpdateTime;
    private Boolean hasEditorSource;
    private Boolean hasEditorSourceExtra;
    private Long instanceNum;
}
