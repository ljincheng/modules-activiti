package cn.booktable.activiti.service.activiti.impl;

import cn.booktable.activiti.entity.activiti.ActComment;
import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActTask;
import cn.booktable.activiti.entity.activiti.ActTimeline;
import cn.booktable.activiti.service.activiti.ActToolService;
import cn.booktable.activiti.service.activiti.ActUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("actToolService")
public class ActToolServiceImpl implements ActToolService {

    private void matchActInstanceUserName(ActInstance actInstance, Map<String, String> userMap){
        if(userMap==null){
            return;
        }
        String userName= userMap.get(actInstance.getUserId());
        actInstance.setUserName(userName);

        List<ActComment> myCommentList= actInstance.getCommentList();
        if(myCommentList!=null){
            myCommentList.stream().forEach(t ->{
                String realName= userMap.get(t.getUserId());
                t.setUserName(realName);
            });
        }

        List<ActTimeline> myTimelineList= actInstance.getTimelineList();
        if(myTimelineList!=null){
            myTimelineList.stream().forEach(t->{
                String realName= userMap.get(t.getUserId());
                t.setUserName(realName);

                if(t.getUsers()!=null && t.getUsers().size()>0){
                    List<String> userNames=new ArrayList<>();
                    for(int i=0,k=t.getUsers().size();i<k;i++){
                        String realnames=userMap.get(t.getUsers().get(i));
                        userNames.add(realnames);
                    }
                    t.setUserNames(userNames);
                }
            });
        }
    }
    @Override
    public void matchInstanceUserName(ActInstance actInstance, ActUserService userService) {

        if(userService==null || actInstance==null){
            return;
        }

        List<String> ids=new ArrayList<>();
        List<ActComment> commentList= actInstance.getCommentList();
        if(commentList!=null){
            commentList.stream().forEach(t ->{
                if(StringUtils.isNotBlank(t.getUserId())) {
                    ids.add(t.getUserId());
                }
            });
        }
        List<ActTimeline> timelineList= actInstance.getTimelineList();
        if(timelineList!=null){
            timelineList.stream().forEach(t->{
                if(StringUtils.isNotBlank(t.getUserId())){
                    ids.add(t.getUserId());
                }
                if(t.getUsers()!=null && t.getUsers().size()>0){
                    ids.addAll(t.getUsers());
                }
            });
        }
        if(StringUtils.isNotBlank(actInstance.getUserId())){
            ids.add(actInstance.getUserId());
        }

        List list=(List) ids.stream().distinct().collect(Collectors.toList());
        Map<String, String> userMap =userService.mapUserName(list);
        matchActInstanceUserName(actInstance,userMap);

    }

    @Override
    public void matchInstanceListUserName(List<ActInstance> actInstanceList, ActUserService userService) {

        if(actInstanceList==null){
            return;
        }
        List<String> ids=new ArrayList<>();
        for(int i=0,k=actInstanceList.size();i<k;i++) {
            ActInstance actInstance=actInstanceList.get(i);
            List<ActComment> commentList = actInstance.getCommentList();
            if (commentList != null) {
                commentList.stream().forEach(t -> {
                    if (StringUtils.isNotBlank(t.getUserId())) {
                        ids.add(t.getUserId());
                    }
                });
            }
            List<ActTimeline> timelineList = actInstance.getTimelineList();
            if (timelineList != null) {
                timelineList.stream().forEach(t -> {
                    if (StringUtils.isNotBlank(t.getUserId())) {
                        ids.add(t.getUserId());
                    }
                    if (t.getUsers() != null && t.getUsers().size() > 0) {
                        ids.addAll(t.getUsers());
                    }
                });
            }
            if (StringUtils.isNotBlank(actInstance.getUserId())) {
                ids.add(actInstance.getUserId());
            }
        }

        List list = (List) ids.stream().distinct().collect(Collectors.toList());
        Map<String, String> userMap =userService.mapUserName(list);
        for(int i=0,k=actInstanceList.size();i<k;i++) {
            ActInstance actInstance=actInstanceList.get(i);
            matchActInstanceUserName(actInstance, userMap);
        }
    }

    @Override
    public void matchActTaskListUserName(List<ActTask> tasks, ActUserService userService) {
        if(tasks==null || tasks.size()==0 || userService==null){
            return;
        }
        List<String> ids=new ArrayList<>();
        for(int i=0,k=tasks.size();i<k;i++) {
            ActTask task = tasks.get(i);
            ids.add( task.getAssignee());
        }
        List list = (List) ids.stream().distinct().collect(Collectors.toList());
        Map<String, String> userMap =userService.mapUserName(list);
        for(int i=0,k=tasks.size();i<k;i++) {
            ActTask task=tasks.get(i);
            String realName=userMap.get( task.getAssignee());
            task.setUserName(realName);
        }
    }
}
