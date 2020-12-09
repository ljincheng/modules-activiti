package cn.booktable.activiti.core;

import lombok.Data;

import java.util.List;

/**
 * 表单
 */
@Data
public class ActForm {
    private String approvalCode;
    private String approvalName;
    private List<ActFormField> form;
    private List<ActFormField> attachments;
}
