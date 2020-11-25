package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.ActApproval;
import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.service.activiti.ActApprovalService;
import cn.booktable.activiti.service.activiti.ActExtendDataService;
import cn.booktable.activiti.service.activiti.ActModelService;
import cn.booktable.activiti.utils.ActivitiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("actApprovalService")
public class ActApprovalServiceImpl implements ActApprovalService {

    private ActExtendDataService actExtendDataService;
    @Autowired
    private ActModelService actModelService;

    public void setActExtendDataService(ActExtendDataService actExtendDataService){
        this.actExtendDataService=actExtendDataService;
    }

    @Override
    public ActResult<ActApproval> create(String approvalCode, String approvalName, String category, String description) {
        ActResult<ActApproval> result=new ActResult<>();


        ActApproval actApproval= actExtendDataService.findActApprovalByApprovalCode(approvalCode);
        if(actApproval!=null){
            ActivitiUtils.setFailResult(result,"ApprovalCode值不能重复使用（"+approvalCode+")");
            return result;
        }

        ActApproval approval=new ActApproval();
        approval.setApprovalCode(approvalCode);
        approval.setApprovalName(approvalName);
        approval.setCategory(category);
        approval.setDescription(description);

        ActModel model=new ActModel();
        model.setKey(approval.getApprovalCode());
        model.setName(approval.getApprovalName());
        model.setDescription(approval.getDescription());
        ActResult<String> createModelResult= actModelService.create(model);
        if(ActivitiUtils.isOkResult(createModelResult)){
            approval.setModelId(createModelResult.getData());
            boolean resultOK=actExtendDataService.insertActApproval(approval);
            if(resultOK){
                ActivitiUtils.setOkResult(result);
                result.setData(approval);
                return result;
            }
        }
        ActivitiUtils.setFailResult(result,"创建失败");
        return result;
    }

    @Override
    public ActResult<String> updateModel(ActApproval approval,String modelJson,String modelSvg) {
        ActResult<String> result=new ActResult<>();
        ActApproval actApproval= actExtendDataService.findActApprovalByApprovalCode(approval.getApprovalCode());
        if(actApproval==null){
            ActivitiUtils.setFailResult(result,"ApprovalCode不存在（"+approval.getApprovalCode()+")");
            return result;
        }
        ActModel actModel=new ActModel();
        actModel.setName(actApproval.getApprovalName());
        actModel.setKey(actApproval.getApprovalCode());
        actModel.setDescription(actApproval.getDescription());
        actModel.setJson(modelJson);
        actModel.setSvg(modelSvg);
        ActResult<String> saveModelResult= actModelService.save(actModel);
        if(ActivitiUtils.isOkResult(saveModelResult)){
            ActivitiUtils.setOkResult(result);
        }else {
            ActivitiUtils.setFailResult(result,"保存失败");
        }
         return result;
    }


    @Override
    public ActResult<String> deploy(String approvalCode) {
        return actModelService.deploy(approvalCode);
    }
}
