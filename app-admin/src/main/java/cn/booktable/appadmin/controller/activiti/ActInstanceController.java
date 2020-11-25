package cn.booktable.appadmin.controller.activiti;

import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActResult;
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

    @PostMapping("/approve/{instanceCode}")
    public JsonView<String> approve(@PathVariable("instanceCode") String instanceCode, String taskId, String comment, String userId){
        JsonView<String> view=new JsonView<String>();
        Map<String,Object> param=new HashMap<>();
        param.put("flag",'Y');
        actInstanceService.approve(taskId,instanceCode,comment,userId,param);
        ViewUtils.submitSuccess(view,messageSource);
        return view;
    }
}
