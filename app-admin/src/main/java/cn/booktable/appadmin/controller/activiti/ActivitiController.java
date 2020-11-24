package cn.booktable.appadmin.controller.activiti;

import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActProcessBo;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.entity.activiti.ActTask;
import cn.booktable.activiti.service.activiti.ActModelService;
import cn.booktable.activiti.service.activiti.ActProcessService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.util.AssertUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/activiti/")
public class ActivitiController {
    private static Logger logger= LoggerFactory.getLogger(ActivitiController.class);

    @Autowired
    private ActProcessService actProcessService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ActModelService actModelService;

    @GetMapping("/list")
    public ModelAndView list(){
        ModelAndView view=new ModelAndView("activiti/list");
        List<ActProcessBo> actProList= actProcessService.processList(null);
        view.addObject("actProList",actProList);
        {
            PageDo<ActProcessBo> page= actProcessService.findDeployList(1L,50,null);
            view.addObject("pagedata",page);
        }

        return view;
    }

    @GetMapping("/deployList")
    public ModelAndView findDeployList(){
        ModelAndView view=new ModelAndView("activiti/deployList");
        PageDo<ActProcessBo> page= actProcessService.findDeployList(1L,50,null);
        view.addObject("pagedata",page);
        return view;
    }


    @GetMapping("/deploy")
    @ResponseBody
    public String deploy(ActProcessBo processBo,String resBpmn){
        processBo.setBpmnFile(String.format("processes/%s.bpmn",resBpmn));
        Boolean result=actProcessService.deploy(processBo);
       if(result!=null)
       {
           return result.toString();
       }
        return "false";
    }


    @PostMapping("/deleteDeploy")
    public JsonView<String> deleteDeploy(String id){
        JsonView<String> result=new JsonView<>();
        try {
            actProcessService.deleteDeploy(id);
            ViewUtils.submitSuccess(result,messageSource);
        }catch (Exception ex)
        {
            logger.error("部署流程失败",ex);
            ViewUtils.pushException(result,messageSource,ex);
        }
        return result;
    }

    @GetMapping("/startProcessInstanceByKey")
    @ResponseBody
    public String startProcessInstanceByKey(String processKey, String businessKey,String flag){
        Map<String, Object> variables=new HashMap<>();
        variables.put("applyuserid", "114");
        variables.put("remark","这是测试");
        if(flag!=null && flag.trim().length()>0) {
            variables.put("flag", flag);
        }
        actProcessService.startProcessInstanceByKey(processKey,variables,businessKey);
        return "ok";
    }

    @GetMapping("/approveInstance")
    @ResponseBody
    public String approveInstance(String taskId, String businessKey, String comments,String userId, String flag){
        Map<String, Object> variables=new HashMap<>();
        variables.put("applyuserid", "114");
        variables.put("remark","这是测试");
        if(flag!=null && flag.trim().length()>0) {
            variables.put("flag", flag);
        }
        if(StringUtils.isBlank(userId))
        {
            userId="114";
        }
        actProcessService.approveInstance(taskId,businessKey,comments,userId,variables);
        return "ok";
    }

    @GetMapping("/instanceList")
    public ModelAndView list(String bizkey,String userName){
        ModelAndView view=new ModelAndView("activiti/instanceList");
        List<ActTask> taskList=null;
        if(StringUtils.isBlank(userName)){
            taskList= actProcessService.instanceList(bizkey);
        }else {
            taskList= actProcessService.instanceList(bizkey,userName);
        }
        view.addObject("taskList",taskList);
        return view;
    }

    @RequestMapping("/imageProcess")
    public void imageProcess(String id, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isNoneBlank(id)) {
            //获取流程图文件流
            InputStream in = actProcessService.findImageProcess(id);

            OutputStream out = null;
            try {
                if(in!=null) {
//                    response.setHeader("Pragma", "no-cache");
//                    response.setHeader("Cache-Control", "no-cache");
//                    response.setDateHeader("Expires", 0);
//                    response.setContentType("image/png");
//                    out=response.getOutputStream();
//
//                    IOUtils.copy(in,out);
//                    response.getOutputStream().flush();



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
            }finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }


}
