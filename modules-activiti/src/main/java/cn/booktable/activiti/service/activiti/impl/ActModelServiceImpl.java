package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActProcessBo;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.service.activiti.ActModelService;
import cn.booktable.activiti.utils.ActivitiUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.*;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service("actModelService")
public class ActModelServiceImpl implements ActModelService{

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ActResult<String> create(ActModel model) {
        ActResult<String> result=new ActResult<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, model.getName());
            modelObjectNode.put(MODEL_REVISION, 1);
            String description = model.getDescription() == null ? "" : model.getDescription();
            modelObjectNode.put(MODEL_DESCRIPTION, description);

            Model newModel = repositoryService.newModel();
            newModel.setMetaInfo(modelObjectNode.toString());
            newModel.setName(model.getName());
            if(model.getCategory()!=null && model.getCategory().trim().length()>0){
                newModel.setCategory(model.getCategory());
            }
            String key = model.getKey() == null ? "" : model.getKey();
            newModel.setKey(key);

            repositoryService.saveModel(newModel);
            repositoryService.addModelEditorSource(newModel.getId(), editorNode.toString().getBytes("utf-8"));
            ActivitiUtils.setOkResult(result);
            result.setData(newModel.getId());
        }catch (UnsupportedEncodingException ex){
            ActivitiUtils.setFailResult(result,ex);
        }
        return result;
    }

    @Override
    public ActResult<String> save(ActModel actModel) {
        ActResult<String> result=new ActResult<>();
        try {
            Model model = repositoryService.getModel(actModel.getId());

            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

            modelJson.put(MODEL_NAME, actModel.getName());
            modelJson.put(MODEL_DESCRIPTION, actModel.getDescription());
            modelJson.put(MODEL_PROCESS_ID, model.getKey());
            model.setMetaInfo(modelJson.toString());
            model.setVersion((model.getVersion()==null?0:model.getVersion())+1);
          //  model.setName(actModel.getName());

            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), actModel.getJson().getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(actModel.getSvg().getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                TranscoderOutput output = new TranscoderOutput(outStream);

                // Do the transformation
                transcoder.transcode(input, output);
                final byte[] ioresult = outStream.toByteArray();
                repositoryService.addModelEditorSourceExtra(model.getId(), ioresult);
            } finally {
                outStream.close();
            }
        }catch (UnsupportedEncodingException ex){
            ActivitiUtils.setFailResult(result,ex);
        }catch (TranscoderException tex){
            ActivitiUtils.setFailResult(result,tex);
        }catch (IOException ioex){
            ActivitiUtils.setFailResult(result,ioex);
        }
        return result;
    }

    @Override
    public ActResult<Boolean> delete(String modelId) {
        ActResult<Boolean> result=new ActResult<>();
        Model model = repositoryService.getModel(modelId);
        if(model!=null) {
            repositoryService.deleteModel(modelId);
        }else {
            ActivitiUtils.setFailResult(result,"找不到数据");
        }
        return result;
    }

    @Override
    public List<ActModel> listAll(String key,String name) {
        List<ActModel> result=new ArrayList<>();
        ModelQuery modelQuery = repositoryService.createModelQuery();
        modelQuery.orderByLastUpdateTime().desc();

        // 条件过滤
        if (key!=null && key.trim().length()>0) {
            modelQuery.modelKey(key);
        }
        if (name!=null && name.trim().length()>0) {
            modelQuery.modelNameLike("%" + name + "%");
        }

        List<Model> lstRecords = modelQuery.list();

        if(lstRecords!=null && lstRecords.size()>0){
            for(int i=0,k=lstRecords.size();i<k;i++){
                Model m=lstRecords.get(i);
                ActModel model=new ActModel();
                model.setId(m.getId());
                model.setCategory(m.getCategory());
                model.setCreateTime(m.getCreateTime());
                model.setLastUpdateTime(m.getLastUpdateTime());
                model.setDeploymentId(m.getDeploymentId());
                model.setKey(m.getKey());
                model.setMetaInfo(m.getMetaInfo());
                model.setTenantId(m.getTenantId());
                model.setVersion(m.getVersion());
                model.setName(m.getName());
                model.setHasEditorSource(m.hasEditorSource());
                model.setHasEditorSourceExtra(m.hasEditorSourceExtra());
                result.add(model);
            }
        }

        return result;
    }

    @Override
   public ActResult<Object> getEditSource(String modelId) {
        ActResult<Object> result=new ActResult<>();

        ObjectNode modelNode = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                String metaInfo=model.getMetaInfo();
                if (metaInfo!=null && metaInfo.length()>0) {
                    modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    //modelNode.put(MODEL_NAME, model.getName());
                }
                modelNode.put(MODEL_NAME, model.getName());
                modelNode.put(MODEL_ID, model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                 JsonNode propertiesNode= editorJsonNode.get("properties");
              if(propertiesNode!=null){
                  ObjectNode propertiesObj=(ObjectNode)propertiesNode;
                  propertiesObj.put(MODEL_PROCESS_ID,model.getKey());
                  propertiesObj.put(MODEL_NAME,model.getName());
              }
                modelNode.put("model", editorJsonNode);

                ActivitiUtils.setOkResult(result);
                result.setData(modelNode);
            } catch (Exception e) {
                ActivitiUtils.setFailResult(result,e);
                //logger.error("Error creating model JSON", e);
              //  throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return result;
    }

    @Override
    public ActResult<String> deploy(String modelId) {
        ActResult<String> result=new ActResult<>();
        try {
            Model modelData = repositoryService.getModel(modelId);
            ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            byte[] bpmnBytes = null;

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            byte[] bytes = repositoryService.getModelEditorSourceExtra(modelData.getId());

            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).key(modelData.getKey())
                    .addBytes(modelData.getName() + ".png", bytes)
                    .addString(processName, new String(bpmnBytes)).deploy();

            ActivitiUtils.setOkResult(result);
            result.setData(deployment.getId());
        }catch (IOException ioex){
            ActivitiUtils.setFailResult(result,ioex);
        }

        return result;
    }

    @Override
    public ActResult<Boolean> deleteDeploy(String id) {
        ActResult<Boolean> result=new ActResult<>();
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if(pd==null){
            ActivitiUtils.setFailResult(result,"找不到数据");
        }else {
            repositoryService.deleteDeployment(pd.getDeploymentId());
            ActivitiUtils.setOkResult(result);
            result.setData(true);
        }
        return result;
    }

    @Override
    public ActResult<InputStream> image(String id) {
        ActResult<InputStream> result=new ActResult<>();
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
            if(pd!=null) {
                InputStream stream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
                result.setData(stream);
                ActivitiUtils.setOkResult(result);
            }else{
                ActivitiUtils.setFailResult(result,"参数无效");
            }

        return result;
    }

    @Override
    public List<ActModel> processListAll(String key, String name) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        if (name!=null && name.trim().length()>0) {
            query.processDefinitionNameLike(name);
        }
        if(key!=null && key.trim().length()>0){
            query.processDefinitionKey(key);
        }
        // 执行查询
        List<ProcessDefinition> list = query.orderByProcessDefinitionVersion().desc().list();
        List<ActModel> result=new ArrayList<>();

        if(list!=null) {
            for (int i = 0; i < list.size(); i++) {
                ProcessDefinition p = list.get(i);
              long instanceNum=  runtimeService.createProcessInstanceQuery().processDefinitionKey(p.getKey()).deploymentId(p.getDeploymentId()).count();
                ActModel processBo = new ActModel();
                processBo.setId(p.getId());
                processBo.setName(p.getName());
                processBo.setCategory(p.getCategory());
                processBo.setDeploymentId(p.getDeploymentId());
                processBo.setKey(p.getKey());
                processBo.setInstanceNum(instanceNum);
                result.add(processBo);
            }
        }

        return result;
    }
}
