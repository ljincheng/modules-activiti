package cn.booktable.activiti.service.activiti.impl;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.core.common.spring.identity.ActivitiUserGroupManagerImpl;
import org.activiti.core.common.spring.identity.ExtendedInMemoryUserDetailsManager;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
public class ActUserGroupManagerImpl implements UserGroupManager {


    private final UserDetailsService userDetailsService;

    public ActUserGroupManagerImpl(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public List<String> getUserGroups(String username) {
        List<String> group=new ArrayList<>();
        group.add(username);
        return group;

    }


    public List<String> getUserRoles(String username) {
        List<String> group=new ArrayList<>();
        group.add(username);
        return group;

    }

    @Override
    public List<String> getGroups() {
        return ((ExtendedInMemoryUserDetailsManager) userDetailsService).getGroups();
    }

    @Override
    public List<String> getUsers() {
        return ((ExtendedInMemoryUserDetailsManager) userDetailsService).getUsers();
    }
}
