package cn.booktable.service.appadmin.aspect;

import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author ljc
 */
@ControllerAdvice
public class AdminControllerAdvice {
    private static Logger logger= LoggerFactory.getLogger(AdminControllerAdvice.class);

    @Autowired
    private MessageSource messageSource;

    /**
     * 应用到@RequestMapping注解方法，在其执行之前初始化数据绑定器
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder){}

    /**
     * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
     * @param model
     */
    @ModelAttribute
    public void addAttributes(Model model)
    {

    }

    @ExceptionHandler(value = Exception.class)
    public JsonView errorHandler(HttpServletRequest request , Exception ex)
    {
        logger.error("异常",ex);
        Locale locale = LocaleContextHolder.getLocale();
       String msg= messageSource.getMessage("i18n.error.internalError",null,locale);
       return JsonView.error(1000,msg);
    }

    @ExceptionHandler(value = SQLException.class)
    public JsonView sqlExceptionHandler(HttpServletRequest request , SQLException ex)
    {
        logger.error("异常",ex);
        Locale locale = LocaleContextHolder.getLocale();
        String msg= messageSource.getMessage("i18n.error.internalError",null,locale);
        return JsonView.error(1001,msg);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public JsonView daoExceptionHandler(HttpServletRequest request , DataIntegrityViolationException ex)
    {
        logger.error("异常",ex);
        Locale locale = LocaleContextHolder.getLocale();
        String msg= messageSource.getMessage("i18n.error.internalError",null,locale);
        return JsonView.error(1002,msg);
    }

    @ExceptionHandler(value = BusinessException.class)
    public JsonView bizExceptionHandler(BusinessException ex)
    {
        return JsonView.error(ex.getCode(),ex.getMessage());
    }

}
