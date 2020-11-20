package cn.booktable.appadmin.controller.kids;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.context.MessageSource;

import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.core.view.JsonView;
import cn.booktable.util.AssertUtils;
import cn.booktable.exception.BusinessException;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.core.page.PageDo;
import cn.booktable.modules.entity.kids.KidsMediaCategoryDo; 
import cn.booktable.modules.service.kids.KidsMediaCategoryService; 

/**
 * 媒体种类
 * @author ljc
 */
@Controller
@RequestMapping("/kids/kidsMediaCategory")
public class KidsMediaCategoryController extends BaseController{

	private static Logger logger= LoggerFactory.getLogger(KidsMediaCategoryController.class);

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private KidsMediaCategoryService kidsMediaCategoryService;
	
	@GetMapping("/list")
	public ModelAndView list(){
		ModelAndView view=new ModelAndView("kids/kidsMediaCategory/list");
		return view;
	}

	@PostMapping("/list")
	@RequiresPermissions("kids:kidsMediaCategory:list")
	public ModelAndView listTable(HttpServletRequest request,@RequestParam(required = false,defaultValue ="1")Long pageIndex,@RequestParam(required = false,defaultValue ="20")Integer pageSize,String startDate,String endDate){
	ModelAndView view=new ModelAndView("kids/kidsMediaCategory/list_table");
		 Map<String,Object> selectItem = getRequestToParamMap(request); 
		 view.addObject("pagedata",kidsMediaCategoryService.queryKidsMediaCategoryListPage(pageIndex,pageSize,selectItem));
		 return view;
	}

	@GetMapping(value="/add")
	public ModelAndView add(HttpServletRequest request){
		ModelAndView view=new ModelAndView("kids/kidsMediaCategory/add");
		return view;
	}

	@PostMapping(value="/add")
	@RequiresPermissions("kids:kidsMediaCategory:add")
	public JsonView<String> add_POST(HttpServletRequest request,KidsMediaCategoryDo kidsMediaCategoryDo){
		JsonView<String> view=new JsonView<String>();
		try{
			Integer dbRes= kidsMediaCategoryService.insertKidsMediaCategory(kidsMediaCategoryDo);
			AssertUtils.isPositiveNumber(dbRes,"操作失败");
			ViewUtils.submitSuccess(view,messageSource);
		}catch (Exception ex) {
			ViewUtils.pushException(view,messageSource,ex);
			logger.error("添加异常",ex);
		}
		return view;
	}

	@GetMapping(value="/view")
	@RequiresPermissions("kids:kidsMediaCategory:view")
	public ModelAndView view(HttpServletRequest request,Long id){
		ModelAndView view =new ModelAndView("kids/kidsMediaCategory/view");
		KidsMediaCategoryDo kidsMediaCategoryDo= kidsMediaCategoryService.findKidsMediaCategoryById(id);
		view.addObject("kidsMediaCategoryDo", kidsMediaCategoryDo);
		return view;
	}

	@GetMapping(value="/edit/{id}")
	public ModelAndView edit(HttpServletRequest request,@PathVariable("id") Long id){
		ModelAndView view =new ModelAndView("kids/kidsMediaCategory/edit");
		KidsMediaCategoryDo kidsMediaCategoryDo= kidsMediaCategoryService.findKidsMediaCategoryById(id);
		view.addObject("kidsMediaCategoryDo", kidsMediaCategoryDo);
		return view;
	}

	@PostMapping(value="/edit/{id}")
	@RequiresPermissions("kids:kidsMediaCategory:edit")
	public JsonView<String> edit_POST(HttpServletRequest request,@PathVariable("id") Long id, KidsMediaCategoryDo kidsMediaCategoryDo){
		JsonView<String> view =new JsonView<String>();
		try{
			kidsMediaCategoryDo.setId(id);
			Integer dbRes= kidsMediaCategoryService.updateKidsMediaCategoryById(kidsMediaCategoryDo);
			AssertUtils.isPositiveNumber(dbRes,"操作失败");
			ViewUtils.submitSuccess(view,messageSource);
		}catch (Exception ex) {
			ViewUtils.pushException(view,messageSource,ex);
			logger.error("编辑异常",ex);
		}
		return view;
	}

	@PostMapping(value="/delete")
	@RequiresPermissions("kids:kidsMediaCategory:delete")
	public JsonView<String> delete(HttpServletRequest request, Long id){
		JsonView<String> view=new JsonView<String>();
		try{
			AssertUtils.notNull(id, "参数无效");
			Integer result=kidsMediaCategoryService.deleteKidsMediaCategoryById(id);
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