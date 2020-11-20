package cn.booktable.appadmin.utils;

import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import org.apache.shiro.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.spring.web.json.Json;

import java.sql.SQLException;
import java.util.Locale;

/**
 * @author ljc
 */
public class ViewUtils {


    public static void submitSuccess(Object view,String subTitle,MessageSource messageSource){
        String msg=messageSource.getMessage("i18n.submitSuccess", null, LocaleContextHolder.getLocale());
        if(view instanceof JsonView)
        {
            JsonView jsonView=((JsonView)view);
            jsonView.setCode(JsonView.CODE_SUCCESS);
            jsonView.setMsg(msg+subTitle);
        }else if(view instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView) view;
            modelAndView.addObject("msg",msg+subTitle);
        }
    }

    public static void submitSuccess(Object view,MessageSource messageSource){
       String msg=messageSource.getMessage("i18n.submitSuccess", null, LocaleContextHolder.getLocale());
        if(view instanceof JsonView)
        {
            JsonView jsonView=((JsonView)view);
            jsonView.setCode(JsonView.CODE_SUCCESS);
            jsonView.setMsg(msg);
        }else if(view instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView) view;
            modelAndView.addObject("msg",msg);
        }
    }
    public static void pushException(Object view, MessageSource messageSource,Exception ex)
    {
        String msg=ex.getMessage();
        int errorCode=0;
        if(ex instanceof DataAccessException || ex instanceof SQLException || ex instanceof DataIntegrityViolationException)
        {
            errorCode=1001;
            msg=messageSource.getMessage("i18n.error.internalError", null, LocaleContextHolder.getLocale());
        }else if(ex instanceof  BusinessException)
        {
            BusinessException bex=(BusinessException)ex;
            errorCode=bex.getCode();
        }
        if(view instanceof JsonView)
        {
            JsonView jsonView=((JsonView)view);
            jsonView.setCode(JsonView.CODE_FAILE);
            jsonView.setErrorCode(errorCode);
            jsonView.setMsg(msg);
        }else if(view instanceof ModelAndView){
            ModelAndView modelAndView=(ModelAndView)view;
            modelAndView.addObject("error", new BusinessException(BusinessException.code_other, msg));
        }
    }
}
