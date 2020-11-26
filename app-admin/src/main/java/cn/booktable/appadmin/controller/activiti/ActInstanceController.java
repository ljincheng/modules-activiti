package cn.booktable.appadmin.controller.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActResult;
import cn.booktable.activiti.entity.activiti.ActTask;
import cn.booktable.activiti.service.activiti.ActInstanceService;
import cn.booktable.activiti.utils.ActivitiUtils;
import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.core.view.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

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
}
