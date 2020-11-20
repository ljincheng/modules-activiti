package cn.booktable.appadmin.controller.shop;

import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.shop.ShopCategoryDo;
import cn.booktable.modules.entity.sys.SysDictDo;
import cn.booktable.modules.service.shop.ShopCategoryService;
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
import java.util.Map;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/shop/category")
public class ShopCategoryController extends BaseController {

    private static Logger logger= LoggerFactory.getLogger(ShopCategoryController.class);
    @Autowired
    private ShopCategoryService shopCategoryService;


    @GetMapping(value = "/list")
    public ModelAndView list() {
        return new ModelAndView("/shop/category/list");
    }

    @PostMapping(value = "/list")
    @RequiresPermissions("shop:category:list")
    public ModelAndView listTable(HttpServletRequest request,String startDate,String endDate,@RequestParam(required = false,defaultValue ="1") Long pageIndex,@RequestParam(required = false,defaultValue ="20")Integer pageSize) {
        ModelAndView model = new ModelAndView("/shop/category/list_table");
        try {
            Map<String,Object> selectItem = getRequestToParamMap(request);
            selectItem.put("isValid", SysDictDo.ISVALID_T);
            model.addObject("pagedata",shopCategoryService.queryShopCategoryListPage(pageIndex,pageSize,selectItem));
        } catch (BusinessException e) {
            setPromptException(model, e);
        } catch (Exception e) {
            logger.error("获取系统信息字典列表异常", e);
            setPromptException(model, e);
        }
        return model;
    }

    @RequestMapping(value="/add",method= RequestMethod.GET)
    public ModelAndView add(HttpServletRequest request)
    {
        ModelAndView view=new ModelAndView("/shop/category/add");
        try{
            view.addObject("shopCategory", null);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("添加商品分类异常（GET）",e);
        }
        return view;
    }

    @RequestMapping(value="/add",method= RequestMethod.POST)
    public ModelAndView add_POST(HttpServletRequest request, ShopCategoryDo shopCategoryDo)
    {
        ModelAndView view=new ModelAndView("/shop/category/add");
        try{
            view.addObject("shopCategory", shopCategoryDo);
            Integer dbRes= shopCategoryService.insertShopCategory(shopCategoryDo);
            AssertUtils.isPositiveNumber(dbRes,"操作失败");
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("添加商品分类异常（POST）",e);
        }
        return view;
    }

    @RequestMapping(value="/view",method=RequestMethod.GET)
    public ModelAndView view(HttpServletRequest request,Long id)
    {
        ModelAndView view =new ModelAndView("/shop/category/view");
        try{
            ShopCategoryDo shopCategory= shopCategoryService.findShopCategoryById(id);
            view.addObject("shopCategory", shopCategory);
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("浏览商品分类异常（GET）",e);
        }
        return view;
    }

    @RequestMapping(value="/edit/{id}",method=RequestMethod.GET)
    public ModelAndView edit(HttpServletRequest request,@PathVariable("id") Long id)
    {
        ModelAndView view =new ModelAndView("/shop/category/edit");
        try{
            ShopCategoryDo shopCategory= shopCategoryService.findShopCategoryById(id);
            view.addObject("shopCategory", shopCategory);
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("编辑商品分类异常（GET）",e);
        }
        return view;
    }

    @RequestMapping(value="/edit/{id}",method=RequestMethod.POST)
    public ModelAndView edit_POST(HttpServletRequest request,@PathVariable("id") Long id, ShopCategoryDo shopCategoryDo)
    {
        ModelAndView view =new ModelAndView("exams/addExamPaper");
        try{
            shopCategoryDo.setId(id);
            Integer result= shopCategoryService.updateShopCategoryById(shopCategoryDo);
            AssertUtils.isTrue(result.equals(1), "保存失败");
            setPromptMessage(view, "操作成功");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("编辑商品分类异常（POST）",e);
        }finally {
            view.addObject("shopCategory", shopCategoryDo);
        }
        return view;
    }

    @PostMapping(value="/delete")
    public JsonView<String> delete(HttpServletRequest request, Long id)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            AssertUtils.notNull(id, "参数无效");
            Integer result=shopCategoryService.deleteShopCategoryById(id);
            AssertUtils.isPositiveNumber(result,"操作失败");
            setPromptMessage(view, view.CODE_SUCCESS, "操作成功");
        }catch (BusinessException e) {
            setPromptException(view, e);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("删除考卷异常",e);
        }
        return view;
    }

}
