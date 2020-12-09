package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.core.ActApprovalTemplate;
import cn.booktable.activiti.core.ActForm;
import cn.booktable.activiti.core.ActFormField;
import cn.booktable.activiti.service.activiti.ActFormService;
import cn.booktable.activiti.utils.ActivitiUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ActFormServiceImpl implements ActFormService {

    @Autowired
    private HistoryService historyService;
    @Autowired
    private ActApprovalTemplate approvalTemplate;

    @Override
    public ActForm findInstanceForm(String instanceCode) {
        Map<String,Object> instanceForm=null;
        ActForm actForm=null;
        HistoricProcessInstance historicProcessInstance= historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(instanceCode).singleResult();
        if(historicProcessInstance!=null) {
            List<HistoricVariableInstance> formList = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstance.getId()).variableName(ActivitiUtils.INSTANCE_VAR_FORM).list();
            if (formList != null && formList.size() > 0) {
                Object formObj = formList.get(0).getValue();
                instanceForm = ActivitiUtils.convertJsonObjectToMap(formObj);
            }
            actForm= approvalTemplate.getForm(historicProcessInstance.getProcessDefinitionKey());
            if(actForm!=null){
                List<ActFormField> fields= actForm.getForm();
                if(fields!=null && instanceForm!=null){
                    for(int i=0,k=fields.size();i<k;i++){
                        ActFormField field=fields.get(i);
                        Object value= instanceForm.get(field.getId());
                        field.setValue(value);
                    }
                }

                List<ActFormField> attachments= actForm.getAttachments();
                if(attachments!=null && instanceForm!=null){
                    for(int i=0,k=attachments.size();i<k;i++){
                        ActFormField field=attachments.get(i);
                        Object value= instanceForm.get(field.getId());
                        field.setValue(value);
                    }
                }

            }
        }
        return actForm;
    }
}
