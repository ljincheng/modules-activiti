package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.core.ActForm;

/**
 *  审批表单
 */
public interface ActFormService {

    ActForm findInstanceForm(String instanceCode);

}
