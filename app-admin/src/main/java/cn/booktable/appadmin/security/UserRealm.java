package cn.booktable.appadmin.security;

import cn.booktable.core.shiro.SessionUtils;
import cn.booktable.core.shiro.SysUserPrimaryPrincipal;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.service.sys.SysParamService;
import cn.booktable.modules.service.sys.SysUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import cn.booktable.core.shiro.Oauth2Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ljc
 */
@Component
public class UserRealm extends AuthorizingRealm {
    private static Logger log= LoggerFactory.getLogger(UserRealm.class);
    @Autowired
    private SysUserService sysUserService;
    @Resource
    private SysParamService sysParamService;

    public UserRealm()
    {
        this.setAuthenticationTokenClass(Oauth2Token.class);
    }


//    @Autowired
//    private SysUserService sysUserService;

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
        Object principalObj= authcToken.getPrincipal();
        Object credentialsObj=authcToken.getCredentials();
//        if(authcToken instanceof UsernamePasswordToken) {
//            UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        SysUserDo user = new SysUserDo();
            user.setUserName(principalObj.toString());
            user.setPassword(credentialsObj.toString());
            if ("666666".equalsIgnoreCase(user.getUserName()) && "666666".equalsIgnoreCase(user.getPassword())) {
                user.setId(10000);
                AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(user, user.getPassword(), getName());
                List<Session> list = SessionUtils.getSessionListByUserName((SysUserPrimaryPrincipal) user);
                if (null != list && list.size() > 0) {
                    DefaultWebSessionManager sessionManager = SessionUtils.getSessionManager();
                    for (Session session : list) {
                        sessionManager.getSessionDAO().delete(session);//消掉session
                    }
                }
                return authcInfo;
            }

//
//        SysUserDo user = sysUserService.findSysUserByUserName(token.getUsername());
//        if (null != user) {
//            if (user.getStatus() == null) {
//                throw new LockedAccountException("账户使用状态无效");
//            } else if (user.getStatus().intValue() == 0) {
//                throw new LockedAccountException("账户注销状态");
//            }
//            if (user.getLocked() != null && user.getLocked().equals(2)) {
//                throw new LockedAccountException("账户已锁定");
//            }
//            String ERROR_PWD_TIME = "5";
//            int months = Integer.parseInt(ERROR_PWD_TIME);
//
//            if (user.getIncorrectTime() != null && user.getIncorrectTime().intValue() > months) {
//                throw new ExcessiveAttemptsException("密码错误次数过多，账户已被锁定");
//            }
//            String psw = String.valueOf(token.getPassword());
//            user.setIp(token.getHost());
//            if (!psw.equals(user.getPassword())) {
//                Integer incorrectTime = user.getIncorrectTime();
//                if (incorrectTime == null) {
//                    incorrectTime = 1;
//                } else {
//                    incorrectTime = incorrectTime + 1;
//                }
//                user.setIncorrectTime(incorrectTime);
//                sysUserService.updateLoginExcessiveAttemptsByUserId(user);
//            } else {
//                user.setIncorrectTime(0);
//                sysUserService.updateLoginExcessiveAttemptsByUserId(user);
//            }
//            AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(user, user.getPassword(), user.getId() + "");
//            List<Session> list = SessionUtils.getSessionListByUserName(token.getUsername());
//            if (null != list && list.size() > 0) {
//                DefaultWebSessionManager sessionManager = SessionUtils.getSessionManager();
//                for (Session session : list) {
//                    sessionManager.getSessionDAO().delete(session);//消掉session
//                }
//            }
//
//            return authcInfo;
//
//        }
        return null;
    }

    //@Override
//    public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
//        CredentialsMatcher credentials = (token, info) -> {
//            UsernamePasswordToken userToken = (UsernamePasswordToken) token;
//            //登录密码
//            String loginPassword = new String(userToken.getPassword());
//            //数据库里，加密后的密码
//            String dbPassword = info.getCredentials().toString();
//
//            return PasswordUtils.matches(loginPassword, dbPassword);
//        };
//
//        super.setCredentialsMatcher(credentials);
//    }
}
