package cn.booktable.appadmin.aspect;

import cn.booktable.modules.annotation.ActionLog;
import cn.booktable.modules.entity.sys.SysActionLogDo;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.service.sys.SysActionLogService;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author ljc
 */
@Aspect
@Component
public class ActionLogAspect {

    @Autowired
    private SysActionLogService sysActionLogService;

    @Pointcut("@annotation(cn.booktable.modules.annotation.ActionLog)")
    public void logPointCut(){
    }

    public Object around(ProceedingJoinPoint point)throws Throwable{
        long beginTime=System.currentTimeMillis();
        try{
            Object result=point.proceed();
            long time=System.currentTimeMillis()-beginTime;
            saveLog(point,time,true);
            return result;
        }catch (Exception ex)
        {
            long time=System.currentTimeMillis()-beginTime;
            saveLog(point,time,false);
            throw ex;
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint,long time,boolean status)throws Exception{

        MethodSignature signature=(MethodSignature)joinPoint.getSignature();
        Method method=joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),signature.getParameterTypes());
        ActionLog annotation=method.getAnnotation(ActionLog.class);

        if(annotation!=null) {
            RequestAttributes requestAttributes= RequestContextHolder.getRequestAttributes();
            HttpServletRequest request=((ServletRequestAttributes) requestAttributes).getRequest();

            SysActionLogDo actionLogDo = new SysActionLogDo();
            actionLogDo.setMode(annotation.mode());
            if(status) {
                actionLogDo.setContent(JSON.toJSONString(request.getParameterMap()));
            }else{
                actionLogDo.setContent(annotation.content());
            }
            Subject subject = SecurityUtils.getSubject();
            if(subject!=null && subject.getPrincipal()!=null )
            {
                SysUserDo sysUserDo=  (SysUserDo) subject.getPrincipal();
                actionLogDo.setUserId(sysUserDo.getId());
            }
            actionLogDo.setActionUrl(request.getRequestURI());
            actionLogDo.setDetail(annotation.detail());
            actionLogDo.setLevel(annotation.level().getText());
            sysActionLogService.insertSysActionLog(actionLogDo);
        }
    }
}
