package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActResult;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.util.List;

public interface ActModelService {

    public String MODEL_ID = "modelId";
    public String MODEL_NAME = "name";
    public String MODEL_REVISION = "revision";
    public String MODEL_DESCRIPTION = "description";

    ActResult<String> create(ActModel model);

    ActResult<String> save(ActModel model);

    ActResult<Boolean> delete(String modelId);

    List<ActModel> listAll(String key,String name);

    ActResult<Object> getEditSource(String modelId);

    ActResult<String> deploy(String modelId);

    ActResult<Boolean> deleteDeploy(String id);

    ActResult<InputStream> image(String id);

    List<ActModel> processListAll(String key,String name);


}
