package cn.booktable.appadmin.controller.sys;

import cn.booktable.core.page.PageDo;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.SysActionLogDo;
import cn.booktable.modules.service.sys.SysActionLogService;
import cn.booktable.appadmin.controller.base.BaseController;
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
@RequestMapping("/sys/actionlog")
public class SysActionLogController extends BaseController {

    private static Logger logger= LoggerFactory.getLogger(SysActionLogController.class);
    @Autowired
    private SysActionLogService sysActionLogService;

    /**
     * 用户活动日志数据分页
     * @return
     */
    @RequestMapping(value="/list",method= RequestMethod.GET)
    public ModelAndView sysActionLogList_methodGet()
    {
        ModelAndView view=new ModelAndView("sys/actionlog/list");
        return view;
    }

    /**
     * 用户活动日志数据分页
     * @param request
     * @param pageIndex
     * @param pageSize
     * @param startDate
     * @param endDate
     * @return
     */
    @RequestMapping(value="/list",method=RequestMethod.POST)
    public ModelAndView sysActionLogList_methodPost(HttpServletRequest request, Long pageIndex, Integer pageSize, String startDate, String endDate)
    {
        ModelAndView view=new ModelAndView("sys/actionlog/list_table");
        try{
            Map<String, Object> selectItem=getRequestToParamMap(request);
            setDateBetweemToMap(selectItem, startDate, endDate);
            pageIndex=pageIndex==null?1L:pageIndex;
            pageSize=pageSize==null?20:pageSize;
            PageDo<SysActionLogDo> pagedata= sysActionLogService.querySysActionLogListPage(pageIndex,pageSize, selectItem);
            view.addObject("pagedata",pagedata);

        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("获取用户活动日志数据分页异常", e);
        }
        return view;
    }



    /**
     * 浏览用户活动日志
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value="/view",method=RequestMethod.GET)
    public ModelAndView viewSysActionLog_methodGet(HttpServletRequest request,Long id)
    {
        ModelAndView view =new ModelAndView("sys/actionlog/view");
        try{
            SysActionLogDo sysActionLog= sysActionLogService.findSysActionLogById(id);
            view.addObject("sysActionLog", sysActionLog);
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("浏览用户活动日志异常（GET）",e);
        }
        return view;
    }
}
