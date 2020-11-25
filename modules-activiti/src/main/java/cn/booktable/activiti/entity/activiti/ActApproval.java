package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 *  审批定义
 */
@Data
public class ActApproval {
    private String approvalCode;//审批定义Code
    private String approvalName;//名称
    private String category;//类别
    private Date createTime;//创建时间
    private Date updateTime;//更新时间
    private Integer status;//状态：1有效，0无效
    private Integer lastVersion;//版本
    private String description;//备注
    private String modelId;//模板ID


}
