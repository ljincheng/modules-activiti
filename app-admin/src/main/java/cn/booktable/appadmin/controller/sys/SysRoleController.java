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
import cn.booktable.modules.entity.sys.SysRoleDo;
import cn.booktable.modules.service.sys.SysPermissionService;
import cn.booktable.modules.service.sys.SysRoleService;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


/**
 * 角色
 * @author ljc
 * @version 1.0
 */
@Controller
@RequestMapping(value="/sys/role/")
public class SysRoleController extends BaseController {

    private static Logger logger=LoggerFactory.getLogger(SysRoleController.class);

    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysPermissionService sysPermissionService;


    /**
     * 角色列表（全部数据）
     * @return
     */
    @RequestMapping(value="/list",method=RequestMethod.GET)
    @RequiresPermissions("sys:role:list")
    public ModelAndView roleListAll(HttpServletRequest request)
    {
        ModelAndView view=new ModelAndView("sys/role/list");
        try{
            Map<String, Object> selectItem=new HashMap<String, Object>();
            List<SysRoleDo> datalist=	sysRoleService.querySysRoleList(selectItem);
            view.addObject("datalist", datalist);
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("获取角色列表异常", e);
        }
        return view;
    }

    @RequestMapping(value="/list",method=RequestMethod.POST)
    @RequiresPermissions("sys:role:list")
    public ModelAndView roleListAll_MethodPOST(HttpServletRequest request,String name,Integer available,Long pageIndex,Integer pageSize,String startDate,String endDate)
    {
        ModelAndView view=new ModelAndView("sys/role/list_table");
        try{
            Map<String, Object> selectItem=new HashMap<String, Object>();
            setDateBetweemToMap(selectItem, startDate, endDate);
            selectItem.put("name", name);
            selectItem.put("available", available);
            pageIndex=pageIndex==null?1L:pageIndex;
            pageSize=pageSize==null?20:pageSize;
            PageDo<SysRoleDo> pagedata= sysRoleService.querySysRoleListPage(pageIndex,pageSize, selectItem);
            view.addObject("pagedata",pagedata);

        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("获取角色列表异常", e);
        }
        return view;
    }

    @RequestMapping(value="/edit",method=RequestMethod.GET)
    @RequiresPermissions("sys:role:edit")
    public ModelAndView editRole_method_get(HttpServletRequest request, String id)
    {
        ModelAndView view=new ModelAndView("sys/role/edit");

        try{
            SysRoleDo sysRole=null;
            if(StringUtils.isNotEmpty(id))
            {
                sysRole=sysRoleService.findSysRoleById(id);
            }
            view.addObject("sysRole", sysRole);
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("新增角色初始页面异常", e);
        }
        return view;
    }

    @RequestMapping(value="/edit",method=RequestMethod.POST)
    @RequiresPermissions("sys:role:edit")
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_role_edit",detail = "修改角色")
    public ModelAndView editRole_method_post(HttpServletRequest request,SysRoleDo sysRole)
    {
        ModelAndView view=new ModelAndView("sys/role/edit");
        try{
            AssertUtils.isNotBlank(sysRole.getName(), "角色名称不能为空");
            AssertUtils.isNotBlank(sysRole.getId(), "参数错误");
            if(sysRole.getAvailable()==null)
            {
                sysRole.setAvailable(0);
            }
            if(StringUtils.isNotEmpty(sysRole.getId()))
            {
                //检查权限

                SysRoleDo oldRole=sysRoleService.findSysRoleById(sysRole.getId());
                AssertUtils.notNull(oldRole,"无效请求参数");
                sysRoleService.updateSysRoleById(sysRole);
            }
            setPromptMessage(view, "操作成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("新增角色异常", e);
        }finally {
            view.addObject("sysRole", sysRole);
        }
        return view;
    }

    @RequestMapping(value="/add",method=RequestMethod.GET)
    @RequiresPermissions("sys:role:add")
    public ModelAndView addRole_method_get(HttpServletRequest request,String id)
    {
        ModelAndView view=new ModelAndView("sys/role/add");
        try{
            SysRoleDo sysRole=null;
            if(StringUtils.isNotEmpty(id))
            {
                sysRole=sysRoleService.findSysRoleById(id);
            }
            view.addObject("sysRole", sysRole);
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("新增角色初始页面异常", e);
        }
        return view;
    }

    @RequestMapping(value="/add",method=RequestMethod.POST)
    @ActionLog(level = ActionLogLevel.IMPORTANT,mode = "sys_role_add",detail = "新增角色")
    public ModelAndView addRole_method_post(HttpServletRequest request,SysRoleDo sysRole)
    {
        ModelAndView view=new ModelAndView("sys/role/add");
        try{
            AssertUtils.isNotBlank(sysRole.getName(), "角色名称不能为空");
            if(sysRole.getAvailable()==null)
            {
                sysRole.setAvailable(0);
            }
            if(StringUtils.isNotEmpty(sysRole.getId()))
            {
                //检查权限
                checkPermission("sys:role:edit");
                SysRoleDo oldRole=sysRoleService.findSysRoleById(sysRole.getId());
                sysRoleService.updateSysRoleById(sysRole);
            }else{
                //检查权限
                checkPermission("sys:role:add");
                sysRoleService.insertSysRole(sysRole);
            }
            view.addObject("sysRole", sysRole);
            setPromptMessage(view, "操作成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("新增角色异常", e);
        }
        return view;
    }

    @RequestMapping(value="/delete",method=RequestMethod.POST)
    @RequiresPermissions("sys_role_delete")
    @ActionLog(level = ActionLogLevel.DANGER,mode = "sys_role_delete",detail = "删除角色")
    public JsonView<String> deleteRole_post(HttpServletRequest request, String id)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            //权限检验
            AssertUtils.isNotBlank(id, "参数错误");

            if(StringUtils.isNotEmpty(id))
            {
                SysRoleDo oldRole=sysRoleService.findSysRoleById(id);
                AssertUtils.notNull(oldRole,"无效的请求");
                sysRoleService.deleteSysRoleById(id);
            }
            setPromptMessage(view,view.CODE_SUCCESS, "操作成功");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("删除角色异常", e);
        }
        return view;

    }

    @RequestMapping(value="/userRoles",method=RequestMethod.GET)
    public ModelAndView roleListWithUserId(Integer userId)
    {
        ModelAndView view =new ModelAndView("sys/role/userRoles");
        try{
            List<SysRoleDo> roleList= sysRoleService.getRoleListWithUserId(userId);
            view.addObject("roleList", roleList);
            view.addObject("userId", userId);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("根据用户ID获取所有角色列表异常", e);
        }
        return view;
    }

    @RequestMapping(value="/permissionMap",method=RequestMethod.GET)
    public ModelAndView permissionMap(HttpServletRequest request,String roleId)
    {
        ModelAndView view=new ModelAndView("sys/role/permissionMap");
        try{
//			PermissionNodeHandler permissionNodeHandler=new PermissionNodeHandler();
//			permissionNodeHandler.setPermissionList( sysRoleService.queryPermissionByRoleId(roleId));
//			sysPermissionService.findChildMenuList(permissionNodeHandler, null);

            view.addObject("permissionNodeList", sysPermissionService.findPermissionNode(roleId));

            view.addObject("roleId",roleId);
        }catch (Exception e) {
            logger.error("获取权限地图异常",e);
            setPromptException(view, e);
        }
        return view;
    }

    @RequestMapping(value="/permissionMap",method=RequestMethod.POST)
    public ModelAndView rolePermissionMap_methodPost(HttpServletRequest request)
    {
        ModelAndView view=new ModelAndView("sys/role/permissionMap");
        try{
            String roleId=request.getParameter("roleId");
            logger.info("roleId:{}",roleId);
            String[] permissionIds=request.getParameterValues("permissionIds");
            Long[] permissionIdArr=null;
            if(permissionIds!=null && permissionIds.length>0)
            {
                permissionIdArr=new Long[permissionIds.length];
                for(int i=0,k=permissionIds.length;i<k;i++)
                {
                    String permissionId=permissionIds[i];
                    permissionIdArr[i]=Long.valueOf(permissionId);
                    logger.info("permissionId:{}",permissionId);
                }
            }
            sysRoleService.saveRolePermissions(roleId, permissionIdArr);
            view.addObject("roleId",roleId);

//			PermissionNodeHandler permissionNodeHandler=new PermissionNodeHandler();
//			permissionNodeHandler.setPermissionList( sysRoleService.queryPermissionByRoleId(roleId));
//			sysPermissionService.findChildMenuList(permissionNodeHandler, null);

                view.addObject("permissionNodeList", sysPermissionService.findPermissionNode(roleId));
//			view.addObject("permissionNodeList", sysPermissionService.findPermissionNode(roleId));
            setPromptMessage(view, "操作成功");
        }catch (Exception e) {
            logger.error("角色权限关系保存异常", e);
        }
        return view;
    }

}
