package cn.booktable.appadmin.controller.sys;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.annotation.ActionLog;
import cn.booktable.modules.annotation.ActionLogLevel;
import cn.booktable.modules.entity.sys.SysDictDo;
import cn.booktable.modules.service.sys.SysDictService;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * 系统信息字典
 * @author ljc
 * @version
 */
@Controller
@RequestMapping("/sys/dict")
public class SysDictController extends BaseController {

    private static Logger log=LoggerFactory.getLogger(SysDictController.class);

    /** 系统信息字典服务  */
    @Resource
    private SysDictService sysDictService;

//    /** redis */
//    @Resource
//    private RedisSystemService redisSystemService;


    @GetMapping(value = "/list")
    public ModelAndView list() {
        return new ModelAndView("sys/dict/list");
    }


    @PostMapping(value = "/list")
    @RequiresPermissions("sys:dict:list")
    public ModelAndView listData(HttpServletRequest request) {
        ModelAndView model = new ModelAndView("sys/dict/list_table");
        try {
            Map<String,Object> selectItem = getRequestToParamMap(request);
            selectItem.put("isValid", SysDictDo.ISVALID_T);
            model.addObject("pagedata",sysDictService.queryListPage(selectItem));
        } catch (BusinessException e) {
            setPromptException(model, e);
        } catch (Exception e) {
            log.error("获取系统信息字典列表异常", e);
            setPromptException(model, e);
        }
        return model;
    }

    /**
     *
     * preShow:新增修改显示页面. <br>
     * @param codeId 字典ID
     * @return
     */
    @GetMapping("/preShow")
    public ModelAndView preShow(String codeId){
        ModelAndView model = new ModelAndView("sys/dict/edit");
        try{
            if(StringUtils.isNotEmpty(codeId)){
                checkPermission("sys:dict:edit");
                model.addObject("model",sysDictService.queryById(codeId));
            }else{
                checkPermission("sys:dict:add");
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
     * add:新增系统信息字典. <br>
     * @param sysDictDo 字典对象
     * @return
     */
    @PostMapping("/add")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_dict_add",detail = "新增系统字典")
    @ResponseBody
    public JsonView<SysDictDo> add(HttpServletRequest request, SysDictDo sysDictDo){
        JsonView<SysDictDo> view = new JsonView<SysDictDo>();
        try {
            checkPermission("sys:dict:add");
            sysDictDo.setIsValid(SysDictDo.ISVALID_T);
            Integer model = sysDictService.insert(sysDictDo);
            if(model>0){
                String codeType = sysDictDo.getCodeType();
//				redisSystemService.setKeyPrefix(RedisKeyConst.JCPT_DICT_SYS);
//                redisSystemService.update(codeType,sysDictService.queryCodeList(codeType));
            }
            setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("新增系统信息字典提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "新增系统信息字典提交异常");
        }
        return view;
    }

    /**
     *
     * update:修改系统信息字典. <br>
     * @param sysDictDo 字典对象
     * @return
     */
    @PostMapping("/edit")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_dict_edit",detail = "修改系统字典")
    @RequiresPermissions("sys:dict:edit")
    @ResponseBody
    public JsonView<SysDictDo> update(HttpServletRequest request, SysDictDo sysDictDo){
        JsonView<SysDictDo> view = new JsonView<SysDictDo>();
        try {
            Integer model = sysDictService.update(sysDictDo);
            if(model>0){
                String codeType = sysDictDo.getCodeType();
//                redisSystemService.update(codeType,sysDictService.queryCodeList(codeType));
            }
            setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("修改系统信息字典提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "修改系统信息字典提交异常");

        }
        return view;
    }

    /**
     *
     * delete:删除系统信息字典. <br>
     */
    @PostMapping( "/delete")
    @ActionLog(level = ActionLogLevel.DANGER,mode = "sys_dict_delete",detail = "删除系统字典")
    @RequiresPermissions("sys:dict:delete")
    @ResponseBody
    public JsonView<SysDictDo> delete(HttpServletRequest request, String codeId,String codeType){
        JsonView<SysDictDo> view = new JsonView<SysDictDo>();
        try {
            Integer model = sysDictService.delete(codeId);
            if(model>0){
//                redisSystemService.delete(codeType);
            }
            setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("删除系统信息字典提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "删除系统信息字典提交异常");
        }
        return view;
    }

    @RequestMapping(value = "/updateCache.do", produces = "text/plain;charset=UTF-8")
    @RequiresPermissions("sys:dict:cache")
    @ResponseBody
    public JsonView<SysDictDo> updateCache(){
        JsonView<SysDictDo> view = new JsonView<SysDictDo>();
        try {
            List<SysDictDo> list = sysDictService.queryAll();
            Map<String,List<SysDictDo>> map = new HashMap<String, List<SysDictDo>>();
            for(SysDictDo dict : list){
                List<SysDictDo> dicts = null;
                String  codeType = dict.getCodeType();
                if(map.containsKey(codeType)){
                    dicts = map.get(codeType);
                    dicts.add(dict);
                }else{
                    dicts = new ArrayList<SysDictDo>();
                    dicts.add(dict);
                }
                map.put(codeType, dicts);
            }
//            for (Map.Entry<String, List<SysDictDo>> entry : map.entrySet()) {
//                redisSystemService.update(entry.getKey(),entry.getValue());
//            }
            view.setCode(JsonView.CODE_SUCCESS);
            view.setMsg("更新缓存成功");
        } catch (BusinessException e) {
            setPromptException(view, e);
        } catch (Exception e) {
            log.error("更新缓存成功提交异常", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "更新缓存失败");
        }
        return view;
    }
}
