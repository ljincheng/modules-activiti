package cn.booktable.appadmin.controller.mobile;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import cn.booktable.exception.BusinessException;
import cn.booktable.core.view.JsonView;

import cn.booktable.core.page.PageDo;
import cn.booktable.modules.entity.mobile.MobileViewPageDo;
import cn.booktable.modules.service.mobile.MobileViewPageService;

/**
 *
 * @author ljc
 * @version v1.0
 */
@Controller
@RequestMapping("/mobile/mobileViewPage")
public class MobileViewPageController extends BaseController{

    private static Logger logger= LoggerFactory.getLogger(MobileViewPageController.class);

    @Autowired
    private MobileViewPageService mobileViewPageService;
    @Autowired
    private MessageSource messageSource;


    @GetMapping("/list")
    public ModelAndView list(){
        ModelAndView view=new ModelAndView("/mobile/mobileViewPage/list");
        return view;
    }

    @PostMapping("/list")
    @RequiresPermissions("mobile:mobileViewPage:list")
    public ModelAndView listTable(HttpServletRequest request,@RequestParam(required = false,defaultValue ="1")Long pageIndex,@RequestParam(required = false,defaultValue ="20")Integer pageSize,String startDate,String endDate){
        ModelAndView view=new ModelAndView("/mobile/mobileViewPage/list_table");
        Map<String,Object> selectItem = getRequestToParamMap(request);
        view.addObject("pagedata",mobileViewPageService.queryMobileViewPageListPage(pageIndex,pageSize,selectItem));
        return view;
    }

    @GetMapping("/add")
    public ModelAndView add(HttpServletRequest request){
        ModelAndView view=new ModelAndView("/mobile/mobileViewPage/add");
        return view;
    }

    @PostMapping("/add")
    @RequiresPermissions("mobile:mobileViewPage:add")
    public ModelAndView add_POST(HttpServletRequest request,MobileViewPageDo mobileViewPageDo){
        ModelAndView view=new ModelAndView("/mobile/mobileViewPage/add");
        try{
            Integer dbRes= mobileViewPageService.insertMobileViewPage(mobileViewPageDo);
            AssertUtils.isPositiveNumber(dbRes,"操作失败");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception ex) {
            logger.error("编辑异常",ex);
            setPromptException(view, ex,messageSource);
        }
        return view;
    }

    @GetMapping(value="/view")
    @RequiresPermissions("mobile:mobileViewPage:view")
    public ModelAndView view(HttpServletRequest request,Long id){
        ModelAndView view =new ModelAndView("/mobile/mobileViewPage/view");
        MobileViewPageDo mobileViewPageDo= mobileViewPageService.findMobileViewPageById(id);
        view.addObject("mobileViewPageDo", mobileViewPageDo);
        return view;
    }

    @GetMapping(value="/edit")
    public ModelAndView edit(HttpServletRequest request,Long id){
        ModelAndView view =new ModelAndView("/mobile/mobileViewPage/edit");
        MobileViewPageDo mobileViewPageDo= mobileViewPageService.findMobileViewPageById(id);
        view.addObject("mobileViewPageDo", mobileViewPageDo);
        return view;
    }

    @PostMapping(value="/edit/{id}")
    @RequiresPermissions("mobile:mobileViewPage:edit")
    public ModelAndView edit_POST(HttpServletRequest request,@PathVariable("id") Long id, MobileViewPageDo mobileViewPageDo){
        ModelAndView view =new ModelAndView("/mobile/mobileViewPage/edit");
        try{
            mobileViewPageDo.setId(id);
            Integer dbRes= mobileViewPageService.updateMobileViewPageById(mobileViewPageDo);
            view.addObject("mobileViewPageDo", mobileViewPageDo);
            AssertUtils.isPositiveNumber(dbRes,"操作失败");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            logger.error("编辑异常",e);
            setPromptException(view, e);
        }
        return view;
    }

    @PostMapping(value="/delete")
    @RequiresPermissions("mobile:mobileViewPage:delete")
    public JsonView<String> delete(HttpServletRequest request, Long id){
        JsonView<String> view=new JsonView<String>();
        try{
            AssertUtils.notNull(id, "参数无效");
            Integer result=mobileViewPageService.deleteMobileViewPageById(id);
            AssertUtils.isPositiveNumber(result,"操作失败");
            setPromptMessage(view, view.CODE_SUCCESS, "操作成功");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception ex) {
            logger.error("删除异常",ex);
            setPromptException(view, ex);
        }
        return view;
    }

}