package cn.booktable.appadmin.controller.sys;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.annotation.ActionLog;
import cn.booktable.modules.annotation.ActionLogLevel;
import cn.booktable.modules.entity.sys.SysActionLogDo;
import cn.booktable.modules.entity.sys.SysPermissionDo;
import cn.booktable.modules.service.sys.SysActionLogService;
import cn.booktable.modules.service.sys.SysPermissionService;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 权限资源
 * @author ljc
 *
 */
@Controller
@RequestMapping("/sys/permission")
public class SysPermissionController extends BaseController {

    private static Logger log=LoggerFactory.getLogger(SysPermissionController.class);
    @Autowired
    private SysPermissionService sysPermissionService;

    @GetMapping("/list")
    public ModelAndView listPermission(HttpServletRequest request)
    {
        ModelAndView view=new ModelAndView("sys/permission/list");
        return view;
    }

    @PostMapping("/list")
    @RequiresPermissions("sys:permission:list")
    public ModelAndView listPermissionData(HttpServletRequest request,Long pageIndex,Integer pageSize,Integer dataType,Long parentId)
    {
        ModelAndView view=new ModelAndView("sys/permission/list_table");
        try{
            //查询
            pageIndex=pageIndex==null?1L:pageIndex;
            pageSize=pageSize==null?20:pageSize;
            Map<String, Object> selectItem=new HashMap<String, Object>();
            selectItem.put("name", request.getParameter("name"));
            selectItem.put("dataType", dataType);
            selectItem.put("perCode", request.getParameter("perCode"));
//			 selectItem.put("parentId", parentId==null?0:parentId);
            PageDo<SysPermissionDo> result= sysPermissionService.queryPermissionListPage(pageIndex, pageSize, selectItem);
            view.addObject("pagedata",result);
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            log.error("获取权限资源列表异常", e);
            setPromptException(view, e);
        }
        return view;
    }

    @RequestMapping(value="/edit",method=RequestMethod.GET)
    public ModelAndView editPermission_page(Long id)
    {
        ModelAndView view=new ModelAndView("system/editPermission");
        try{
            view.addObject("id", id);
            SysPermissionDo sysPermission= sysPermissionService.findSysPermissionById(id);
            view.addObject("sysPermission",sysPermission);
        }catch (Exception e) {
            log.error("编辑权限资源异常", e);
        }
        return view;
    }

    @RequestMapping(value="/edit",method=RequestMethod.POST)
    @RequiresPermissions("sys:permission:edit")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode ="sys_permission_edit",detail = "修改菜单")
    public ModelAndView editPermission(HttpServletRequest request, SysPermissionDo sysPermission)
    {
        ModelAndView view=new ModelAndView("system/editPermission");
        try{

            //返回页面表单信息
            view.addObject("sysPermission", sysPermission);

            //表单信息过滤
            AssertUtils.isNotBlank(sysPermission.getName(),"名称不能为空");
            AssertUtils.notNull(sysPermission.getId(),"无效参数");
            Integer dataType= sysPermission.getDataType();
            if(dataType!=null)
            {
                if(dataType.equals(1))
                {
                    AssertUtils.isNotBlank(sysPermission.getUrl(),"菜单链接地址不能为空");
                }else if(dataType.equals(2))
                {
                    AssertUtils.isNotBlank(sysPermission.getPerCode(),"权限代码不能为空");
                }
            }
            //保存表单信息
            sysPermission.setAvailable(1);//默认生效

            sysPermissionService.updateSysPermissionBaseInfoById(sysPermission);
            //业务执行成功，页面提示
            setPromptMessage(view, "修改成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            log.error("编辑权限异常", e);
            setPromptException(view, e);
        }
        return view;
    }

    @RequestMapping(value="/add",method=RequestMethod.GET)
    public ModelAndView addPerimission_methodGet(Long parentId)
    {
        ModelAndView view=new ModelAndView("system/addPermission");
        try{
            view.addObject("parentId", parentId);
        }catch (Exception e) {
            log.error("新增权限资源异常",e);
            setPromptException(view, e);
        }
        return view;
    }

    @RequestMapping(value="/add",method=RequestMethod.POST)
    @RequiresPermissions("sys:permission:add")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_permission_add",detail = "新增菜单")
    public ModelAndView addPerimission_methodPost(HttpServletRequest request,SysPermissionDo sysPermission)
    {
        ModelAndView view=new ModelAndView("system/addPermission");
        try{
            //返回页面表单信息
            view.addObject("sysPermission", sysPermission);

            //表单信息过滤
            AssertUtils.isNotBlank(sysPermission.getName(),"名称不能为空");
            Integer dataType= sysPermission.getDataType();
            if(dataType!=null)
            {
                if(dataType.equals(1))
                {
                    AssertUtils.isNotBlank(sysPermission.getUrl(),"菜单链接地址不能为空");
                }else if(dataType.equals(2))
                {
                    AssertUtils.isNotBlank(sysPermission.getPerCode(),"权限代码不能为空");
                }
            }
            //保存表单信息
            sysPermission.setAvailable(1);//默认生效
            if(sysPermission.getParentId()==null)
            {
                sysPermission.setParentId(0L);
            }

            sysPermissionService.insertSysPermission(sysPermission);
            //业务执行成功，页面提示
            setPromptMessage(view, "保存成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            log.error("新增权限资源异常", e);
            setPromptException(view, e);
        }
        return view;
    }


    /** 重新设计新增／修改权限**/
    @RequestMapping(value="/editTree",method=RequestMethod.GET)
    @RequiresPermissions("sys:permission:edit")
    public ModelAndView editPermissionTree_page(Long parentId)
    {
        ModelAndView view=new ModelAndView("sys/permission/editTree");
        try{
            view.addObject("parentId", parentId);
            SysPermissionDo sysPermission= sysPermissionService.findSysPermissionById(parentId);
            view.addObject("sysPermission",sysPermission);

        }catch (Exception e) {
            log.error("编辑权限资源异常", e);
        }
        return view;
    }

    @RequestMapping(value="/editTree",method=RequestMethod.POST)
    @RequiresPermissions("sys:permission:edit")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_permission_tree_edit",detail = "修改菜单")
    public ModelAndView editPermissionTree_methodPost(HttpServletRequest request,SysPermissionDo sysPermission,Integer parentId)
    {
        ModelAndView view=new ModelAndView("sys/permission/editTree");
        try{
            //权限验证
            //返回页面表单信息
            //view.addObject("sysPermission", sysPermission);

            //表单信息过滤
            AssertUtils.isNotBlank(sysPermission.getName(),"名称不能为空");
            AssertUtils.notNull(sysPermission.getId(),"无效参数");
            Integer dataType= sysPermission.getDataType();
            if(dataType!=null)
            {
                if(dataType.equals(1))
                {
                    AssertUtils.isNotBlank(sysPermission.getUrl(),"菜单链接地址不能为空");
                }else if(dataType.equals(2))
                {
                    AssertUtils.isNotBlank(sysPermission.getPerCode(),"权限代码不能为空");
                }
            }
            //保存表单信息
            sysPermission.setAvailable(1);//默认生效

            sysPermissionService.updateSysPermissionBaseInfoById(sysPermission);
            //业务执行成功，页面提示
            setPromptMessage(view, "修改成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            log.error("编辑权限异常", e);
            setPromptException(view, e);
        }finally {
            //返回页面表单信息
            view.addObject("sysPermission", sysPermission);
            view.addObject("parentId", parentId);
        }
        return view;
    }

    @RequestMapping(value="/addTree",method=RequestMethod.POST)
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_permission_tree_add",detail = "添加菜单")
    @RequiresPermissions("sys:permission:add")
    public ModelAndView addPerimissionTree_methodPost(HttpServletRequest request,SysPermissionDo sysPermission,Integer parentId)
    {
        ModelAndView view=new ModelAndView("sys/permission/editTree");
        try{
            //返回页面表单信息
            view.addObject("sysPermission", sysPermission);

            //表单信息过滤
            AssertUtils.isNotBlank(sysPermission.getName(),"名称不能为空");
            Integer dataType= sysPermission.getDataType();
            if(dataType!=null)
            {
                if(dataType.equals(1))
                {
                    AssertUtils.isNotBlank(sysPermission.getUrl(),"菜单链接地址不能为空");
                }else if(dataType.equals(2))
                {
                    AssertUtils.isNotBlank(sysPermission.getPerCode(),"权限代码不能为空");
                }
            }
            //保存表单信息
            sysPermission.setAvailable(1);//默认生效
            if(sysPermission.getParentId()==null)
            {
                sysPermission.setParentId(0L);
            }
            sysPermission.setId(null);
            sysPermissionService.insertSysPermissionByUser(sysPermission,currentUser());
            //业务执行成功，页面提示
            setPromptMessage(view, "保存成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            log.error("新增权限资源异常", e);
            setPromptException(view, e);
        }finally {
            //返回页面表单信息
            view.addObject("sysPermission", sysPermission);
            view.addObject("parentId", parentId);
        }
        return view;
    }
    /*** end **/


    @RequestMapping("/menuView")
    public ModelAndView menuView(HttpServletRequest request){
        ModelAndView model = new ModelAndView("sys/permission/menuView");
        return model;
    }

    @RequestMapping(value = "/menuData", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String menuJsonData()
    {
        try{
            JSONArray result=new JSONArray();
            List<JSONObject> list=sysPermissionService.jsonObjectChildMenuListByUser(null,currentUser());
            if(list!=null)
            {
                log.info("获取总数据记录数有："+list.size());
                for(JSONObject item:list)
                {
                    result.add(item);
                }
            }
            JSONObject root=new JSONObject();
            root.put("title", "系统菜单");
            root.put("children", result);
            root.put("dataType", 3);
            root.put("open", true);
            root.put("url", "javascript:menuUrl(0)");
            String jsonData=root.toJSONString();
            log.info("菜单数据："+jsonData);
            return jsonData;
        }catch (Exception e) {
            log.error("获取JSON菜单数据异常", e);
        }
        return null;

    }

    @RequestMapping(value="/delete")
    @RequiresPermissions("sys:permission:delete")
    @ActionLog(level = ActionLogLevel.DANGER,mode = "sys_permission_delete",detail = "删除菜单")
    public JsonView<String> deletePerimission(HttpServletRequest request, Long id)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            AssertUtils.notNull(id, "参数无效");
            SysPermissionDo oldPermissionDo=sysPermissionService.findSysPermissionById(id);
            AssertUtils.notNull(oldPermissionDo,"无效请求参数");
            sysPermissionService.deleteSysPermissionById(id);
            setPromptMessage(view, view.CODE_SUCCESS, "操作成功");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            log.error("新增权限资源异常",e);
            setPromptException(view, e);
        }
        return view;
    }


}
