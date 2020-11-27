package cn.booktable.appadmin.controller.activiti;


import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.service.activiti.ActModelService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.util.AssertUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/activiti/model/")
public class ActivitiModelController {
        private static Logger logger= LoggerFactory.getLogger(ActivitiModelController.class);

        @Autowired
        private MessageSource messageSource;
        @Autowired
        private ActModelService actModelService;

    /**
     * 创建模型
     * @param actModel
     * @return
     */
    @RequestMapping("/create")
    public JsonView<String> modelCreate(ActModel actModel){
        JsonView<String> view=new JsonView<>();
        try {
            AssertUtils.isNotBlank(actModel.getName(), "名称不能为空");
            AssertUtils.isNotBlank(actModel.getKey(), "Key不能为空");
            ActResult<String> result = actModelService.create(actModel);
            if(ActivitiUtils.isOkResult(result)){
                ViewUtils.submitSuccess(view,messageSource);
            }else {
                ViewUtils.submitFail(view,result.getMsg());
            }
        }catch (Exception ex)
        {
            ViewUtils.pushException(view,messageSource,ex);
        }
        return view;
    }

    @GetMapping("/list")
    public ModelAndView modelList(String name, String key){
        ModelAndView view=new ModelAndView("activiti/model/list");
        return view;
    }

    @PostMapping("/modelTable")
    public ModelAndView modelTable(String name, String key,Boolean isProcess){
        ModelAndView view=new ModelAndView("activiti/model/model_table");
        try {
            if(isProcess==null){
                isProcess=false;
            }
            view.addObject("isProcess",isProcess);
            if(isProcess==null || !isProcess) {
                List<ActModel> result = actModelService.listAll(key, name);
                view.addObject("modelList", result);
            }else{
                List<ActModel> result = actModelService.processListAll(key, name);
                view.addObject("modelList", result);
            }
        }catch (Exception ex)
        {
            ViewUtils.pushException(view,messageSource,ex);
        }
        return view;
    }

    @RequestMapping(value = "/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object getEditorJson(@PathVariable String modelId) {
        ActResult<Object> result= actModelService.getEditSource(modelId);
        return result.getData();
    }


    @GetMapping(value = {"/stencilset"}, produces = {"application/json;charset=utf-8"})
    @ResponseBody
    public String getStencilset() {
        logger.info("StencilsetRestResource.getStencilset-----------");
        // 文件位置需要跟stencilset.json文件放置的路径匹配,否则进入到在线编辑器页面会是一片空白,没有菜单等显示信息
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("stencilset.json");
        try {
            return IOUtils.toString(inputStream, "utf-8");
        } catch (Exception e) {
            throw new BusinessException("Error while loading stencil set", e);
        }
    }

    /**
     * 模型保存
     * @param modelId
     * @param name
     * @param description
     * @param svg_xml
     * @param json_xml
     * @param request
     * @return
     */
    @RequestMapping(value = "/{modelId}/save", method = RequestMethod.PUT)
//    @ResponseBody
    public JsonView<String> save(@PathVariable String modelId, String name, String description, String svg_xml, String json_xml, HttpServletRequest request) {
        JsonView<String> view=new JsonView<>();
        try {
            ActModel actModel=new ActModel();
            actModel.setId(modelId);
            actModel.setName(name);
            actModel.setDescription(description);
            actModel.setSvg(svg_xml);
            actModel.setJson(json_xml);
            ActResult<String> result=actModelService.save(actModel);
            ViewUtils.submitSuccess(view,messageSource);
        } catch (Exception e) {
            ViewUtils.pushException(view,messageSource,e);
            logger.error("保存流程模型失败，错误信息:{}", e);
        }
        return view;
    }

    /**
     * 删除模型
     * @param modelId
     * @return
     */
    @PostMapping("/delete/{modelId}")
    public JsonView<Boolean> delete(@PathVariable String modelId) {
        JsonView<Boolean> view=new JsonView<>();
        try {

            ActResult<Boolean> result=actModelService.delete(modelId);
            if(ActivitiUtils.isOkResult(result)) {
                ViewUtils.submitSuccess(view, messageSource);
                view.setData(result.getData());
            }else {
                ViewUtils.submitFail(view,result.getMsg());
            }
        } catch (Exception e) {
            ViewUtils.pushException(view,messageSource,e);
            logger.error("保存流程模型失败，错误信息:{}", e);
        }
        return view;

    }

    /**
     * 部署
     * @param modelId
     * @return
     */
    @PostMapping("/deploy/{modelId}")
    public JsonView<String> deploy(@PathVariable String modelId) {
        JsonView<String> view=new JsonView<>();
        try {

            ActResult<String> result=actModelService.deploy(modelId);
            if(ActivitiUtils.isOkResult(result)) {
                ViewUtils.submitSuccess(view, messageSource);
            }else {
                ViewUtils.submitFail(view,result.getMsg());
            }
        } catch (Exception e) {
            ViewUtils.pushException(view,messageSource,e);
            logger.error("保存流程模型失败，错误信息:{}", e);
        }
        return view;

    }

    /**
     * 删除部署
     * @param id
     * @return
     */
    @PostMapping("/deleteDeploy")
    public JsonView<String> deleteDeploy(String id){
        JsonView<String> result=new JsonView<>();
        try {
            actModelService.deleteDeploy(id);
            ViewUtils.submitSuccess(result,messageSource);
        }catch (Exception ex)
        {
            logger.error("部署流程失败",ex);
            ViewUtils.pushException(result,messageSource,ex);
        }
        return result;
    }


    /**
     * 模型图
     * @param id
     * @param request
     * @param response
     */
    @RequestMapping("/image")
    public void image(String id, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isNoneBlank(id)) {
            //获取流程图文件流
            ActResult<InputStream> result=actModelService.image(id);
            if(ActivitiUtils.isOkResult(result)) {
                InputStream in = result.getData();
                try {
                    if (in != null) {
                        byte[] b = new byte[1024];
                        int len;
                        while ((len = in.read(b, 0, 1024)) != -1) {
                            response.getOutputStream().write(b, 0, len);
                        }
                        response.getOutputStream().flush();
                        response.getOutputStream().close();
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{

            }
        }
    }


    @RequestMapping("/exportBpmn/{modelId}")
    public void exportBpmn(@PathVariable String modelId, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isNoneBlank(modelId)) {
            //获取流程图文件流
            ActResult<InputStream> result=actModelService.exportBpmnModel(modelId);
            if(ActivitiUtils.isOkResult(result)) {
                InputStream in = result.getData();
                try {
                    //设置响应头 以下载的方式返回到浏览器
                    response.setHeader("Content-Disposition","attachment;filename="+modelId.trim().replace("-","")+".bpmn20.xml");
                    if (in != null) {
                        byte[] b = new byte[1024];
                        int len;
                        while ((len = in.read(b, 0, 1024)) != -1) {
                            response.getOutputStream().write(b, 0, len);
                        }
                        response.getOutputStream().flush();
                        response.getOutputStream().close();
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{

            }
        }
    }
}
