package cn.booktable.appadmin.controller.sys;

import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.SysParamDo;
import cn.booktable.modules.entity.sys.SysReviewDo;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.service.sys.SysReviewService;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/sys/review/")
public class SysReviewController extends BaseController {
    private static Logger logger= LoggerFactory.getLogger(SysReviewController.class);

    @Autowired
    private SysReviewService sysReviewService;
    private final String TABLENAME_REVIEW="sys_review";;

    /**
     *
     * list:系统参数查询页面. <br>
     * @return
     */
    @GetMapping("/list")
    public ModelAndView list() {
        return new ModelAndView("sys/review/list");
    }

    /**
     *
     * listData:系统参数查询数据. <br>
     * @param request
     * @return
     */
    @PostMapping("/list")
    @RequiresPermissions("sys:param:list")
    public ModelAndView listData(HttpServletRequest request,@RequestParam(value = "pageIndex",required = false,defaultValue = "1")Long pageIndex,@RequestParam(value = "pageSize",required = false,defaultValue = "20")Integer pageSize) {
        ModelAndView model = new ModelAndView("sys/review/list_table");
        try {
            Map<String,Object> selectItem = getRequestToParamMap(request);
            selectItem.put("isValid", SysParamDo.ISVALID_T);

            PageDo<SysReviewDo> pageDo=sysReviewService.querySysReviewListPage(pageIndex,pageSize,selectItem,TABLENAME_REVIEW);
            model.addObject("pagedata",pageDo);
        } catch (BusinessException e) {
            setPromptException(model, e);
        } catch (Exception e) {
            logger.error("获取系统参数列表异常", e);
            setPromptException(model, e);
        }
        return model;
    }

    /**
     * 新增
     * @param request
     * @return
     */
    @RequestMapping(value="/add",method= RequestMethod.GET)
    public ModelAndView add_methodGet(HttpServletRequest request,Long subjectId,@RequestParam(value = "pageIndex",required = false,defaultValue = "1")Long pageIndex,@RequestParam(value = "pageSize",required = false,defaultValue = "20")Integer pageSize)
    {
        ModelAndView view=new ModelAndView("sys/review/add");
        try{
            Map<String,Object> selectItem=new HashMap<>();
            selectItem.put("subjectId",subjectId);
            selectItem.put("reviewId",-1);
            PageDo<SysReviewDo> pageDo=sysReviewService.querySysReviewListPage(pageIndex,pageSize,selectItem,TABLENAME_REVIEW);
            sysReviewService.suppleChildReview(pageDo.getPage(),TABLENAME_REVIEW);
            view.addObject("pagedata", pageDo);
            view.addObject("subjectId",subjectId);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("添加考卷异常（GET）",e);
        }
        return view;
    }

    /**
     * 新增
     * @param request
     * @param sysReviewDo
     * @return
     */
    @RequestMapping(value="/add",method=RequestMethod.POST)
    public JsonView<SysReviewDo> add_methodPost(HttpServletRequest request, SysReviewDo sysReviewDo)
    {
        JsonView<SysReviewDo> view=new JsonView<SysReviewDo>();
        try{
            SysUserDo sysUserDo=currentUser();
            sysReviewDo.setUserId(sysUserDo.getId());
            Integer result= sysReviewService.insertSysReview(sysReviewDo,TABLENAME_REVIEW);
            AssertUtils.isPositiveNumber(result, "数据保存失败");
            setPromptMessage(view, JsonView.CODE_SUCCESS,"操作成功");
            view.setData(sysReviewDo);
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("添加考卷异常（POST）",e);
        }
        return view;
    }

    @RequestMapping(value="/datalist",method=RequestMethod.POST)
    public JsonView<PageDo<SysReviewDo>> add_methodPost(HttpServletRequest request, Long subjectId,@RequestParam(value = "pageIndex",required = false,defaultValue = "1")Long pageIndex,@RequestParam(value = "pageSize",required = false,defaultValue = "20")Integer pageSize) {
        JsonView<PageDo<SysReviewDo>> view = new JsonView<PageDo<SysReviewDo>>();
        try{
            Map<String,Object> selectItem=new HashMap<>();
            selectItem.put("subjectId",subjectId);
            selectItem.put("reviewId",-1);
            PageDo<SysReviewDo> pageDo=sysReviewService.querySysReviewListPage(pageIndex,pageSize,selectItem,TABLENAME_REVIEW);
            sysReviewService.suppleChildReview(pageDo.getPage(),TABLENAME_REVIEW);
            view.setData(pageDo);
            setPromptMessage(view, JsonView.CODE_SUCCESS,"操作成功");
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("添加考卷异常（GET）",e);
        }
        return view;
    }

    /**
     * 新增
     * @param request
     * @param sysReviewDo
     * @return
     */
    @RequestMapping(value="/delete",method=RequestMethod.POST)
    public JsonView<SysReviewDo> delete_methodPost(HttpServletRequest request,Long id, SysReviewDo sysReviewDo)
    {
        JsonView<SysReviewDo> view=new JsonView<SysReviewDo>();
        try{
            Integer result= sysReviewService.deleteSysReviewById(id,TABLENAME_REVIEW);
            AssertUtils.isPositiveNumber(result, "数据删除失败");
            setPromptMessage(view, JsonView.CODE_SUCCESS,"操作成功");
            view.setData(sysReviewDo);
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("添加考卷异常（POST）",e);
        }
        return view;
    }


}
