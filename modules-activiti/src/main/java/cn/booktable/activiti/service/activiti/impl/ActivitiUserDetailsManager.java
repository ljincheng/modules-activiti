package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.service.activiti.ActUser;
import cn.booktable.activiti.service.activiti.ActUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

public class ActivitiUserDetailsManager extends InMemoryUserDetailsManager {


    private ActUserService actUserService;

    public ActivitiUserDetailsManager(ActUserService actUserService){
        this.actUserService=actUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        if(actUserService!=null) {
            ActUser actUser = actUserService.findUser(userId);
//        BizUserEntity bizUser = userService.getBizUserById(userId);
            return new User(actUser.getUserName(), "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER")));
        }else {
            return new User(userId, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER")));
        }
    }


}
