package cn.booktable.appadmin.controller.base;

import cn.booktable.core.constant.SystemConst;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.util.DateUtils;
import cn.booktable.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.dao.DataAccessException;
import org.apache.shiro.subject.Subject;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class BaseController {

    /**
     * 判断是否是系统超级用户。
     * @return
     */
    protected Boolean isSuperSysUser()
    {
        Subject subject = SecurityUtils.getSubject();
        if(subject!=null)
        {
            return  subject.hasRole(SystemConst.SYSTEM_SUPERROLE);
        }
        return false;
    }

    protected SysUserDo currentUser()
    {
        Subject subject = SecurityUtils.getSubject();
        if(subject!=null && subject.getPrincipal()!=null )
        {
            if(subject.getPrincipal() instanceof SysUserDo) {
                return (SysUserDo) subject.getPrincipal();
            }
        }
        return null;

    }

    protected void setPromptMessage(ModelAndView view, String msg)
    {
        view.addObject("promptMsg", msg);
    }


    /**
     * 设置提示业务异常信息
     * @param view
     * @param e
     */
    protected void setPromptException(ModelAndView view, BusinessException e)
    {
        view.addObject("error", e);
    }

    /**
     * 设置异常信息
     * @param view
     * @param e
     */
    protected void setPromptException(ModelAndView view,Exception e)
    {
        if(e instanceof DataAccessException || e instanceof SQLException || e instanceof DataIntegrityViolationException)
        {
            view.addObject("error", new BusinessException(BusinessException.code_other, "Fail [Error:1001]"));
        }else {
            view.addObject("error", new BusinessException(BusinessException.code_other, e.getMessage()));
        }
    }

    protected void setPromptException(ModelAndView view, Exception e, MessageSource messageSource)
    {
        if(e instanceof DataAccessException || e instanceof SQLException || e instanceof DataIntegrityViolationException)
        {
            Locale locale = LocaleContextHolder.getLocale();
            view.addObject("error", new BusinessException(BusinessException.code_other,messageSource.getMessage("i18n.error.internalError", null, locale) ));
        }else {
            view.addObject("error", new BusinessException(BusinessException.code_other, e.getMessage()));
        }
    }

    /**
     * 验证是否有权限操作，如果没有抛出BusinessException权限类型异常。
     * @param code
     * 权限代码。
     */
    protected void checkPermission(String code)
    {
        try{
            SecurityUtils.getSubject().checkPermission(code);
        }catch (AuthorizationException e) {
            throw new BusinessException(BusinessException.code_auth,"您无操作权限。");
        }
    }

    /**
     * 设置查询时间范围
     * @param selectItem
     * @param startDate
     * @param endDate
     */
    protected void setDateBetweemToMap(Map<String, Object> selectItem, String startDate, String endDate)
    {
        try{
            selectItem.put("startDate", StringUtils.isNotBlank(startDate)?(DateUtils.parse(startDate+" 00:00:00")):null);
            selectItem.put("endDate", StringUtils.isNotBlank(endDate)?(DateUtils.parse(endDate+" 23:59:59","yyyy-MM-dd hh:mm:ss")):null);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置异常信息
     * @param view
     * @param e
     */
    protected  <T> void setPromptException(JsonView<T> view, Exception e)
    {
        view.setCode(JsonView.CODE_FAILE);
        if(e instanceof DataAccessException)
        {
            view.setMsg("Fail [Error:1001]");
        }else {
            view.setMsg(e.getMessage());
        }
    }

    /**
     * 设置提示信息
     * @param view
     * @param code
     * @param msg
     */
    protected <T> void setPromptMessage(JsonView<T> view,Integer code, String msg)
    {
        view.setCode(code);
        view.setMsg(msg);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getRequestToParamMap(
            HttpServletRequest request) {

        Map<String, Object> paramMap = new HashMap<>();
        Enumeration<String> keyNames = request.getParameterNames();
        if(StringUtils.isEmpty(request.getParameter("pageIndex"))){
            paramMap.put("pageIndex", "1");
        }
        if(StringUtils.isEmpty(request.getParameter("pageSize"))){
            paramMap.put("pageSize","20");
        }
        while (keyNames.hasMoreElements()) {
            String attrName = keyNames.nextElement();
            String attrValue = request.getParameter(attrName);
            if (StringUtils.isNotEmpty(attrValue)) {
                paramMap.put(attrName, attrValue.trim());
            }
        }
        return paramMap;
    }

}
