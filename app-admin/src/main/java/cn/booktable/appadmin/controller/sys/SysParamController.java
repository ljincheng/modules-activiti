package cn.booktable.appadmin.controller.sys;

import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.annotation.ActionLog;
import cn.booktable.modules.annotation.ActionLogLevel;
import cn.booktable.modules.entity.sys.SysParamDo;
import cn.booktable.modules.service.sys.SysParamService;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/sys/param/")
public class SysParamController extends BaseController {

    private static Logger log= LoggerFactory.getLogger(SysParamController.class);

    /** 系统参数服务 */
    @Resource
    private SysParamService sysParamService;

    /**
     *
     * list:系统参数查询页面. <br>
     * @return
     */
    @GetMapping("/list")
    public ModelAndView list() {
        return new ModelAndView("sys/param/list");
    }

    /**
     *
     * listData:系统参数查询数据. <br>
     * @param request
     * @return
     */
    @PostMapping("/list")
    @RequiresPermissions("sys:param:list")
    public ModelAndView listData(HttpServletRequest request) {
        ModelAndView model = new ModelAndView("sys/param/list_table");
        try {
            Map<String,Object> selectItem = getRequestToParamMap(request);
            selectItem.put("isValid", SysParamDo.ISVALID_T);
            model.addObject("pagedata",sysParamService.queryListPage(selectItem));
        } catch (BusinessException e) {
            setPromptException(model, e);
        } catch (Exception e) {
            log.error("获取系统参数列表异常", e);
            setPromptException(model, e);
        }
        return model;
    }

    /**
     *
     * preShow:新增修改显示页面. <br>
     * @param paramId
     * @return
     */
    @GetMapping( "/preShow")
    public ModelAndView preShow(String paramId){
        ModelAndView model = new ModelAndView("sys/param/edit");
        try{
            if(StringUtils.isNotEmpty(paramId)){
                model.addObject("model",sysParamService.queryById(paramId));
            }else{
            }
        } catch (BusinessException e) {
            setPromptException(model, e);
        } catch (Exception e) {
            log.error("获取系统信息字典记录异常", e);
            setPromptException(model, e);
        }
        return model;

    }

    /**
     *
     * add:新增系统参数. <br>
     * @param param 系统参数对象
     * @return
     */
    @RequestMapping(value = "/add", produces = "text/plain;charset=UTF-8")
    @RequiresPermissions("sys:param:add")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_param_add",detail = "新增系统参数")
    @ResponseBody
    public JsonView<SysParamDo> add(HttpServletRequest request, SysParamDo param){
        JsonView<SysParamDo> view = new JsonView<SysParamDo>();
        try {
            param.setIsValid(SysParamDo.ISVALID_T);
            Integer model = sysParamService.insert(param);
            if(model>0)
            {
                setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
            }else {
                setPromptMessage(view,view.CODE_FAILE, "操作失败");
            }
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("新增系统参数提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "新增系统参数提交异常");
        }
        return view;
    }

    /**
     *
     * update:修改系统参数. <br>
     * @param param 系统参数对象
     * @return
     */
    @PostMapping("/edit")
    @RequiresPermissions("sys:param:edit")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_param_edit",detail = "修改系统参数")
    @ResponseBody
    public JsonView<SysParamDo> update(HttpServletRequest request,SysParamDo param){
        JsonView<SysParamDo> view = new JsonView<SysParamDo>();
        try {
            param.setIsValid(SysParamDo.ISVALID_T);
            Integer model = sysParamService.update(param);
            if(model>0)
            {
                setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
            }else {
                setPromptMessage(view,view.CODE_FAILE, "操作失败");
            }
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("修改系统参数提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "修改系统参数提交异常");
        }
        return view;
    }

    /**
     *
     * delete:删除系统参数. <br>
     * @param paramId 系统参数ID
     * @return
     */
    @PostMapping( "/delete")
    @RequiresPermissions("sys:param:delete")
    @ActionLog(level = ActionLogLevel.DANGER,mode = "sys_param_delete",detail = "删除系统参数")
    @ResponseBody
    public JsonView<SysParamDo> delete(HttpServletRequest request,String paramId){
        JsonView<SysParamDo> view = new JsonView<SysParamDo>();
        try {
            SysParamDo oldParam=sysParamService.queryById(paramId);
            AssertUtils.notNull(oldParam,"无效请求参数");
            Integer model = sysParamService.delete(paramId);
            if(model>0)
            {
                setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
            }else {
                setPromptMessage(view,view.CODE_FAILE, "操作失败");
            }
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("删除系统参数提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "删除系统参数提交异常");
        }
        return view;
    }
}
