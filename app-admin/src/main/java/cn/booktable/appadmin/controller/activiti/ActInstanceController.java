package cn.booktable.appadmin.controller.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActModel;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.entity.activiti.ActTask;
import cn.booktable.activiti.service.activiti.ActInstanceService;
import cn.booktable.activiti.service.activiti.ActModelService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/activiti/instance/")
public class ActInstanceController {
    private static Logger logger= LoggerFactory.getLogger(ActInstanceController.class);

    @Autowired
    private ActInstanceService actInstanceService;

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ActModelService actModelService;

    @GetMapping("/historyList")
    public ModelAndView historyList(String approvalCode,String userId,@RequestParam(required = false,defaultValue ="1")Integer pageIndex,@RequestParam(required = false,defaultValue ="20")Integer pageSize){
        ModelAndView view=new ModelAndView("activiti/instance/historyList");
        Map<String,Object> selected=new HashMap<>();
        if(userId!=null && !userId.isEmpty()){
            selected.put("userId",userId);
        }
        if(StringUtils.isNotBlank(approvalCode))
        {
            selected.put("approvalCode",approvalCode);
        }
        List<ActModel> modelList = actModelService.listAll(null , null);
        view.addObject("modelList",modelList);
        view.addObject("selected",selected);
        return view;
    }
    @PostMapping("/historyListData")
    public ModelAndView historyListData(String approvalCode,String userId,Boolean isHistory,@RequestParam(required = false,defaultValue ="1")Integer pageIndex,@RequestParam(required = false,defaultValue ="20")Integer pageSize){
        ModelAndView view=new ModelAndView("activiti/instance/historyList_table");
        Map<String,Object> selected=new HashMap<>();
        if(userId!=null && !userId.isEmpty()){
            selected.put("userId",userId);
        }
        if(StringUtils.isNotBlank(approvalCode))
        {
            selected.put("approvalCode",approvalCode);
        }
        PageDo<ActInstance> instanceList =null;
        if(isHistory!=null && isHistory.booleanValue()) {
            instanceList = actInstanceService.historyPageList(pageIndex, pageSize, selected);
        }else{
            instanceList=actInstanceService.processPageList(pageIndex,pageSize,selected);
        }
        view.addObject("pagedata", instanceList);
        return view;
    }


    @GetMapping("/list")
    public ModelAndView list(String approvalCode,String deploymentId){
        ModelAndView view=new ModelAndView("activiti/instance/list");
        List<ActInstance> instanceList= actInstanceService.queryListAll(approvalCode,deploymentId);
        view.addObject("instanceList",instanceList);
        return view;
    }

    @GetMapping("/detail")
    public ModelAndView detail(String instanceCode){
        ModelAndView view=new ModelAndView("activiti/instance/detail");
        ActInstance actInstance= actInstanceService.detail(instanceCode);
        view.addObject("actInstance",actInstance);
        return view;
    }


    @GetMapping("/create/{approvalCode}")
    public JsonView<String> create(@PathVariable("approvalCode") String approvalCode,String instanceCode, String name,String userId){
        JsonView<String> view=new JsonView<String>();
        Map<String,Object> variables=new HashMap<>();
        ActResult<String> result=actInstanceService.create(approvalCode,instanceCode,userId,name,variables);
        if(ActivitiUtils.isOkResult(result)) {
            ViewUtils.submitSuccess(view, messageSource);
        }else{
            ViewUtils.submitFail(view,result.getMsg());
        }
        return view;
    }

    /**
     * 审批
     * @param instanceCode
     * @param taskId
     * @param comment
     * @param userId
     * @return
     */
    @PostMapping("/approve/{instanceCode}")
    public JsonView<String> approve(@PathVariable("instanceCode") String instanceCode, String taskId, String comment, String userId,String status){
        JsonView<String> view=new JsonView<String>();
        Map<String,Object> param=new HashMap<>();
       ActResult<Void> actResult= actInstanceService.approve(taskId,instanceCode,status,comment,userId,param);
       if(ActivitiUtils.isOkResult(actResult)) {
           ViewUtils.submitSuccess(view, messageSource);
       }else{
           ViewUtils.submitFail(view,actResult.getMsg());
       }
        return view;
    }

    @GetMapping("/myTask")
    public JsonView<List<ActTask>> myTask( String userId,String groupId){
        JsonView<List<ActTask>> view=new JsonView<List<ActTask>>();
        Map<String,Object> param=new HashMap<>();
        param.put("flag",'Y');
        List<ActTask> taskList= actInstanceService.activeTask(userId,groupId);

        ViewUtils.submitSuccess(view,messageSource);
        view.setData(taskList);

        return view;
    }

    @GetMapping("/activeTask")
    public ModelAndView activeTask(String userId,String groupId){
        ModelAndView view=new ModelAndView("activiti/instance/activeTask");
        List<ActTask> taskList= actInstanceService.activeTask(userId,groupId);
        view.addObject("taskList",taskList);
        return view;
    }

    @RequestMapping("/image/{instanceCode}")
    public void imageProcess(@PathVariable("instanceCode") String instanceCode, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isNoneBlank(instanceCode)) {
            //获取流程图文件流
            InputStream in = actInstanceService.image(instanceCode);

            OutputStream out = null;
            try {
                out =response.getOutputStream();
                if(in!=null) {
                    byte[] b = new byte[1024];
                    int len;
                    while ((len = in.read(b, 0, 1024)) != -1) {
                        out.write(b, 0, len);
                    }
                    out.flush();
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
