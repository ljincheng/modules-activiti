package cn.booktable.appadmin.controller.platform;

import cn.booktable.core.view.JsonView;
import cn.booktable.modules.service.sys.*;
import cn.booktable.appadmin.controller.base.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import cn.booktable.core.constant.SystemConst;
import cn.booktable.cryptojs.CryptoJSUtil;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.AtlantisHtmlMenu;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.entity.sys.SystemDo;
import cn.booktable.appadmin.config.AdminSysConfig;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import cn.booktable.util.VerifyCodeUtils;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/platform")
public class MainController  extends BaseController {
    private static Logger logger= LoggerFactory.getLogger(MainController.class);

    @Autowired
    private AdminSysConfig adminSysConfig;
    @Autowired
    private SysPermissionService sysPermissionService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysAttachmentService sysAttachmentService;

    @RequestMapping(value="/resetPassword", method=RequestMethod.GET)
    public ModelAndView resetPasswordPage()
    {
        ModelAndView model = new ModelAndView("platform/resetPassword");
        return model;
    }

    @RequestMapping(value="/resetPassword", method=RequestMethod.POST)
    public ModelAndView resetPassword(String oldPassword,String password,String password2)
    {
        ModelAndView model = new ModelAndView("platform/resetPassword");
        try{
            AssertUtils.isNotBlank(password, "新密码不能为空");
            AssertUtils.isNotBlank(oldPassword,"原密码不能为空");
            if(!password.equals(password2))
            {
                throw new BusinessException("密码不一致");
            }
            SysUserDo currentUser=currentUser();
            if(currentUser==null)
            {
                throw new BusinessException("请先登录");
            }
            currentUser=sysUserService.findSysUserById(currentUser.getId());
            if(currentUser!=null)
            {
                String psw=DigestUtils.md5Hex(oldPassword+ adminSysConfig.getPasswordKey());
                if(currentUser.getPassword().equals(psw))
                {
                    String newPsw=DigestUtils.md5Hex(password+adminSysConfig.getPasswordKey());
                    sysUserService.resetPassword(currentUser.getId(), newPsw, new Date());
                    setPromptMessage(model, "修改密码成功");
                }else{
                    throw new BusinessException("原密码不正确");
                }
            }else{
                throw new BusinessException("请先登录");
            }
        }catch (BusinessException e) {
            setPromptException(model, e);
        }catch (Exception ex) {
            logger.error("修改密码异常", ex);
            setPromptException(model, ex);
        }
        return model;
    }
    @Autowired
    MessageSource messageSource;

    @GetMapping("/main")
    public ModelAndView platform_main(HttpServletRequest request,
                                      HttpServletResponse response,Integer pid,String lang) {
        ModelAndView model = new ModelAndView("platform/main");

        try {
            Locale locale = LocaleContextHolder.getLocale();
            String msg = messageSource.getMessage("i18n.lang.zh",null,locale);
            model.addObject("langName",msg);

            platformMenuHtmlData(model,pid==null? SystemConst.PLATFORM_DEFAULT:pid);
            viewLoadPhoto(model);

            OperatingSystemMXBean osmx = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            SystemDo dto = new SystemDo();
            dto.setSysTime(System.currentTimeMillis());
            dto.setOsName(System.getProperty("os.name"));
            dto.setOsArch(System.getProperty("os.arch"));
            dto.setOsVersion(System.getProperty("os.version"));
            dto.setUserLanguage(System.getProperty("user.language"));
            dto.setUserDir(System.getProperty("user.dir"));
            dto.setTotalPhysical(osmx.getTotalPhysicalMemorySize()/1024/1024);
            dto.setFreePhysical(osmx.getFreePhysicalMemorySize()/1024/1024);
            dto.setMemoryRate(BigDecimal.valueOf((1-osmx.getFreePhysicalMemorySize()*1.0/osmx.getTotalPhysicalMemorySize())*100).setScale(2, RoundingMode.HALF_UP));
            dto.setProcessors(osmx.getAvailableProcessors());
            dto.setJvmName(System.getProperty("java.vm.name"));
            dto.setJavaVersion(System.getProperty("java.version"));
            dto.setJavaHome(System.getProperty("java.home"));
            dto.setJavaTotalMemory(Runtime.getRuntime().totalMemory()/1024/1024);
            dto.setJavaFreeMemory(Runtime.getRuntime().freeMemory()/1024/1024);
            dto.setJavaMaxMemory(Runtime.getRuntime().maxMemory()/1024/1024);
            dto.setUserName(System.getProperty("user.name"));
            dto.setSystemCpuLoad(BigDecimal.valueOf(osmx.getSystemCpuLoad()*100).setScale(2, RoundingMode.HALF_UP));
            dto.setUserTimezone(System.getProperty("user.timezone"));

            model.addObject("sysInfo",dto);

            // 获取个人通知消息


        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public String platformMenuHtmlData(ModelAndView model ,Integer platformId) {
        String result=null;
        String indexHref=null;
        try {
            StringBuilder sb = new StringBuilder();
            SysUserDo userDo=this.currentUser();
            model.addObject("user",userDo);
            List<AtlantisHtmlMenu> list = null;
            if (this.isSuperSysUser()) {
                MenuListHandler<AtlantisHtmlMenu> handler=new  AtlantisHtmlMenuHandler();
                list = sysPermissionService.findAllPlatformMenuList(handler,platformId);
            }else{
                Integer userId=userDo.getId();
                list =  sysPermissionService.findPlatformMenuList(new AtlantisHtmlMenuHandler(),  userId,platformId);
            }


            if (list != null) {
                logger.debug("获取总数据记录数有：" + list.size());
                for (AtlantisHtmlMenu item :list) {
                    if (item != null) {
                        sb.append(item.toHtml());

                        if(indexHref==null)
                        {
                            if(item.getChildren()!=null && item.getChildren().size()>0) {
                                List<AtlantisHtmlMenu> childs=item.getChildren();
                                for(AtlantisHtmlMenu menu:childs) {
                                    if(StringUtils.isNotBlank( menu.getHref())) {
                                        indexHref = menu.getHref();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            result = sb.toString();
            logger.debug("菜单数据：" + result);

        } catch (Exception e) {
            logger.error("获取html菜单数据异常", e);
        }
        model.addObject("menuHtml", result);
        if(StringUtils.isBlank(indexHref))
        {
            indexHref="about:blank";
        }
        model.addObject("firstMenuHref", indexHref);
        return result;
    }


    private void viewLoadPhoto(ModelAndView model)
    {
        try{
            String photoPath="../res/lib/atlantislite/img/profile.jpg";
            SysUserDo sysUserDo = sysUserService.findSysUserById(currentUser().getId());
            String userPhoto = sysUserDo.getImg();
            if (StringUtils.isNotBlank(userPhoto)) {
                photoPath = "../sys/attachment/file?id=" + userPhoto;

            }
            model.addObject("sysAccountPhoto", photoPath);

        }catch (Exception ex)
        {
            setPromptException(model,ex);
        }
    }

    @RequestMapping(value="/resetUserPhoto", method=RequestMethod.GET)
    public ModelAndView resetUserPhoto_get()
    {
        ModelAndView model = new ModelAndView("platform/resetAccountPhoto");
        try{
            viewLoadPhoto(model);

        }catch (Exception ex)
        {
            setPromptException(model,ex);
        }
        return model;
    }


    @RequestMapping(value="/resetUserPhoto",method= RequestMethod.POST)
    public JsonView<String> userPhotoChange_methodPost(HttpServletRequest request, HttpServletResponse response, String photo)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            String img=request.getParameter("photo");
            AssertUtils.isNotBlank(img,"参数无效");
            SysUserDo sysUserDo=new SysUserDo();
            sysUserDo.setImg(img);
            sysUserDo.setId(currentUser().getId());
            Integer dbRes= sysUserService.updateUserPhoto(sysUserDo);
            AssertUtils.isTrue(dbRes!=null && dbRes.intValue()>0,"保存失败");
            //更新附件使用状态。
            sysAttachmentService.updateCheckedStatusPass(Long.valueOf(img),currentUser().getId());
            setPromptMessage(view, JsonView.CODE_SUCCESS,"保存成功");
            view.setData(img);
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            logger.error("修改用户头像异常", e);
            setPromptException(view, e);
        }
        return view;
    }
}
