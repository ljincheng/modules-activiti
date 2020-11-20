package cn.booktable.appadmin.controller.sys;

import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.annotation.ActionLog;
import cn.booktable.modules.annotation.ActionLogLevel;
import cn.booktable.modules.entity.sys.SysLockDo;
import cn.booktable.modules.service.sys.SysLockService;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/sys/lock")
public class SysLockController extends BaseController {

    private static Logger logger= LoggerFactory.getLogger(SysLockController.class);

    @Autowired
    private SysLockService sysLockService;


    /**
     * 防止并发锁表数据分页
     * @return
     */
    @RequestMapping(value="/list",method= RequestMethod.GET)
    public ModelAndView lockList_methodGet()
    {
        ModelAndView view=new ModelAndView("sys/lock/list");
        return view;
    }

    /**
     * 防止并发锁表数据分页
     * @param request
     * @param pageIndex
     * @param pageSize
     * @param startDate
     * @param endDate
     * @return
     */
    @RequestMapping(value="/list",method=RequestMethod.POST)
    @RequiresPermissions("sys:lock:list")
    public ModelAndView lockList_methodPost(HttpServletRequest request, Long pageIndex, Integer pageSize, String startDate, String endDate)
    {
        ModelAndView view=new ModelAndView("sys/lock/list_table");
        try{
            //权限检验
            Map<String, Object> selectItem=new HashMap<>();
            setDateBetweemToMap(selectItem, startDate, endDate);
            pageIndex=pageIndex==null?1L:pageIndex;
            pageSize=pageSize==null?20:pageSize;
            PageDo<SysLockDo> pagedata= sysLockService.queryLockListPage(pageIndex,pageSize, selectItem);
            view.addObject("pagedata",pagedata);

        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("获取防止并发锁表数据分页异常", e);
        }
        return view;
    }


    /**
     * 删除防止并发锁表
     * @param request
     * @param lockNum
     * @return
     */
    @RequestMapping(value="/delete")
    @ActionLog(level = ActionLogLevel.DANGER,mode = "sys_lock_delete",detail = "删除业务锁")
    @RequiresPermissions("sys:lock:delete")
    public JsonView<String> deleteLock_methodPost(HttpServletRequest request, String lockNum)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            //权限检验
            AssertUtils.isNotBlank(lockNum, "参数无效");
            Integer result=sysLockService.deleteLockByLockNum(lockNum);
            AssertUtils.isTrue(result.equals(1),"数据库操作失败");
            setPromptMessage(view, view.CODE_SUCCESS, "操作成功");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("删除防止并发锁表异常",e);
        }
        return view;
    }
}
