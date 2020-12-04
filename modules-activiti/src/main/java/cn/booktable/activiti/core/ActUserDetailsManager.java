package cn.booktable.activiti.core;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

public class ActUserDetailsManager extends InMemoryUserDetailsManager {


//    private ActUserService actUserService;

//    public ActUserDetailsManager(ActUserService actUserService){
//        this.actUserService=actUserService;
//    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

//        if(actUserService!=null) {
//            ActUser actUser = actUserService.findUser(userId);
////        BizUserEntity bizUser = userService.getBizUserById(userId);
//            return new User(actUser.getUserName(), "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER")));
//        }else {
            return new User(userId, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER")));
//        }
    }


}
