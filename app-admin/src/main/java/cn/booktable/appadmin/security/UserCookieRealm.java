package cn.booktable.appadmin.security;

import cn.booktable.core.constant.SystemConst;
import cn.booktable.core.shiro.Oauth2Token;
import cn.booktable.core.shiro.SessionUtils;
import cn.booktable.core.shiro.SysUserPrimaryPrincipal;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.service.sys.SysParamService;
import cn.booktable.modules.service.sys.SysUserService;
import cn.booktable.util.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserCookieRealm extends AuthorizingRealm {
    private static Logger log= LoggerFactory.getLogger(UserCookieRealm.class);

    @Autowired
    private SysUserService sysUserService;
    @Resource
    private SysParamService sysParamService;

    public UserCookieRealm()
    {
        //this.setAuthenticationTokenClass(Oauth2Token.class);
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        if(token instanceof Oauth2Token)
        {
            return true;
        }
        return super.supports(token);
    }

    /**
     * 授权(验证权限时调用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        try{
//			Object principal = principals.getPrimaryPrincipal();
            SysUserDo user = (SysUserDo)principals.getPrimaryPrincipal();
//			SysUserDo user=sysUserService.findSysUserByUserName((String)principal);
            if(user!=null && (user.getLocked()==null || user.getLocked().intValue()!=2))//账户存在，且不是锁定状态
            {
                Integer userId=user.getId();

                List<String> roleList = sysUserService.getRoleStrListByUserId(userId);
                List<String> permissionList = sysUserService.getPermissionCodeStrListByUserId(userId);

                //为当前用户设置角色和权限
                SimpleAuthorizationInfo simpleAuthorInfo = new SimpleAuthorizationInfo();
                simpleAuthorInfo.addRoles(roleList);
                simpleAuthorInfo.addStringPermissions(permissionList);

                return simpleAuthorInfo;
            }
        }catch (Exception e) {
            log.error("获取用户认证权限异常",e);
        }
        return null;
    }

    /**
     * 认证(登录时调用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authcToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken)authcToken;

        SysUserDo user=sysUserService.findSysUserByUserName(token.getUsername());
        if (null != user) {
            if(user.getStatus()==null)
            {
                throw new LockedAccountException("账户使用状态无效");
            }else if(user.getStatus().intValue()==0)
            {
                throw new LockedAccountException("账户注销状态");
            }
            if(user.getLocked()!=null && user.getLocked().equals(2))
            {
                throw new LockedAccountException("账户已锁定");
            }
            String ERROR_PWD_TIME = sysParamService.queryValueByCode("ERROR_PWD_TIME");
            ERROR_PWD_TIME = StringUtils.isBlank(ERROR_PWD_TIME) ? "5" : ERROR_PWD_TIME;
            int months = Integer.parseInt(ERROR_PWD_TIME);

            if(user.getIncorrectTime()!=null && user.getIncorrectTime().intValue()>months)
            {
                throw new ExcessiveAttemptsException("密码错误次数过多，账户已被锁定");
            }
            String psw=String.valueOf( token.getPassword());
            user.setIp(token.getHost());
            if(!psw.equals(user.getPassword()))
            {
                Integer incorrectTime=user.getIncorrectTime();
                if(incorrectTime==null)
                {
                    incorrectTime=1;
                }else{
                    incorrectTime=incorrectTime+1;
                }
                user.setIncorrectTime(incorrectTime);
                sysUserService.updateLoginExcessiveAttemptsByUserId(user);
            }else{
                user.setIncorrectTime(0);
                sysUserService.updateLoginExcessiveAttemptsByUserId(user);
            }
            AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(user, user.getPassword(), getName());

            List<Session> list = SessionUtils.getSessionListByUserName((SysUserPrimaryPrincipal)user);
            if(null != list && list.size() > 0){
                DefaultWebSessionManager sessionManager = SessionUtils.getSessionManager();
                for(Session session : list){
                    sessionManager.getSessionDAO().delete(session);//消掉session
                }
            }

            return authcInfo;
        }

        return null;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        boolean result=this.hasRole(principals, SystemConst.SYSTEM_SUPERROLE);
        if(!result)
        {
            return super.isPermitted(principals, permission);
        }
        return result;
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        if(principals!=null)
        {
            SysUserDo user=(SysUserDo) principals.getPrimaryPrincipal();
            return user.getId();
        }
        return principals;
    }

}
