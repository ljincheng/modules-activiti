package cn.booktable.appadmin.config;

import cn.booktable.core.redis.RedisManager;
import cn.booktable.core.shiro.RedisCacheManager;
import cn.booktable.core.shiro.RedisSessionDAO;
import cn.booktable.extras.thymeleaf.shiro.dialect.ShiroDialect;
import cn.booktable.appadmin.security.UserCookieRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import cn.booktable.core.shiro.Oauth2Cookie;
import cn.booktable.core.shiro.Oauth2Filter;
import cn.booktable.appadmin.security.UserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.servlet.Filter;
import java.util.*;

/**
 * @author ljc
 */
@Configuration
public class ShiroConfig {


    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
        template.setConnectionFactory(factory);
        //template.setKeySerializer(new StringRedisSerializer());
        //template.setValueSerializer(new RedisObjectSerializer());
        return template;
    }

    @Bean
    public RedisManager redisManager( @Qualifier("redisTemplate") RedisTemplate redisTemplate,@Value("${booktable.redisGroup:booktable}")String group)
    {
        RedisManager redisManager=new RedisManager(redisTemplate,group);
        return redisManager;
    }

    /**
     * 单机环境，session交给shiro管理
     */
    @Bean
    public SessionManager sessionManager(@Value("${booktable.shiro.globalSessionTimeout:3600}") long globalSessionTimeout, @Value("${booktable.shiro.cookieName:token}") String cookieName
    , @Qualifier("redisManager") RedisManager redisManager){

        cn.booktable.core.shiro.SessionManager sessionManager=new cn.booktable.core.shiro.SessionManager(globalSessionTimeout,cookieName,redisManager)  ;
        return sessionManager;
    }

    /**
     * 集群环境，session交给spring-session管理
     */
    @Bean
    public ServletContainerSessionManager servletContainerSessionManager() {
        return new ServletContainerSessionManager();
    }

    @Bean("securityManager")
    public SecurityManager securityManager(@Value("${booktable.shiro.globalSessionTimeout:3600}") long globalSessionTimeout, UserCookieRealm cookieRealm, SessionManager sessionManager,@Qualifier("redisManager") RedisManager redisManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(cookieRealm);
//        List<Realm> realms=new ArrayList<>();
       // realms.add(userRealm);
//        realms.add(cookieRealm);
//        securityManager.setRealms(realms);
        securityManager.setSessionManager(sessionManager);
        securityManager.setRememberMeManager(null);

        // 关闭shiro自带的session
//        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
//        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
//        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
//        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
//        securityManager.setSubjectDAO(subjectDAO);
        RedisCacheManager cacheManager= new RedisCacheManager(redisManager,globalSessionTimeout);
        securityManager.setCacheManager(cacheManager);
        return securityManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);
        shiroFilter.setLoginUrl("/login");
        //这里的/index是后台的接口名,非页面,登录成功后要跳转的链接
        shiroFilter.setSuccessUrl("/platform/main");
        //未授权界面,该配置无效，并不会进行页面跳转
        shiroFilter.setUnauthorizedUrl("/");

        Map<String, Filter> filters=new HashMap<>();
        Oauth2Filter oauth2Filter=new Oauth2Filter();
        filters.put("oauth2",oauth2Filter);
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
//        filterMap.put("/swagger/**", "anon");
//        filterMap.put("/v2/api-docs", "anon");
//        filterMap.put("/swagger-ui.html", "anon");
//        filterMap.put("/webjars/**", "anon");
//        filterMap.put("/swagger-resources/**", "anon");

        filterMap.put("/statics/**", "anon");
        filterMap.put("/res/**", "anon");
        filterMap.put("/login", "anon");
        filterMap.put("/favicon.ico", "anon");
        filterMap.put("/captcha", "anon");
        filterMap.put("/api/**", "oauth2");
        filterMap.put("/**", "authc");
//        filterMap.put("/**", "anon");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter;
    }

    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * Thymeleaf-extras-shiro标签配置
     * @return
     */
    @Bean
    public ShiroDialect shiroDialect()
    {
        return new ShiroDialect();
    }

}
