package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActResult;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface ActModelService {

    public String MODEL_ID = "modelId";
    public String MODEL_NAME = "name";
    public String MODEL_NAMESPACE = "process_namespace";
    public String MODEL_REVISION = "revision";
    public String MODEL_DESCRIPTION = "description";

    public String MODEL_PROCESS_ID="process_id";

    ActResult<String> create(ActModel model);

    ActResult<String> save(ActModel model);

    void delete(String modelId,boolean enforce);

    List<ActModel> listAll(String key,String name,String category);

    ActResult<Object> getEditSource(String modelId);

    ActResult<String> deploy(String modelId);

    ActResult<Boolean> deleteDeploy(String id);

    ActResult<InputStream> image(String id);

    List<ActModel> processListAll(String key,String name,String category);


    ActResult<InputStream> exportBpmnModel(String modelId);
    ActResult<Void> importBpmnModel(String modelId,InputStream stream);
}
