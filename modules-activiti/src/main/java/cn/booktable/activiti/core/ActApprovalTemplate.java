package cn.booktable.activiti.core;

import lombok.Data;

import java.util.Map;

@Data
public class ActApprovalTemplate {


    private Map<String, ActForm> approval;

    public ActForm getForm(String code){
        if(approval==null){
            return null;
        }
        ActForm form=approval.get(code);
        return form;
    }

}
