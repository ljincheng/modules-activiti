package cn.booktable.activiti.service.activiti;

import cn.booktable.activiti.entity.activiti.ActInstanceMetaInfo;
import cn.booktable.activiti.service.activiti.impl.ActIdentityServiceImpl;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

public class ActInstanceMetaInfoHelper {
    public static final String INSTANCE_VAR_METAINFO="metainfo";
    public static final String INSTANCE_VAR_VARIABLES="variables";
    public static ActInstanceMetaInfo newInstanceVariables( String userId, String userName, String status){
        ActInstanceMetaInfo variables=new ActInstanceMetaInfo();
        variables.setUserId(userId);
        variables.setUserName(userName);
        variables.setStatus(status);
        return variables;
    }

    public static  Map<String,Object> createInstanceVariables(String userId,String userName,String status,Map<String,Object> variables){
        Map<String ,Object> instanceVariables=new HashMap<>();
        instanceVariables.put(INSTANCE_VAR_METAINFO,newInstanceVariables(userId,userName,status));
        instanceVariables.put(INSTANCE_VAR_VARIABLES,variables);
        instanceVariables.put("actIdentityService",new ActIdentityServiceImpl());
        return instanceVariables;
    }

    public static ActInstanceMetaInfo getMetaInfo(Map<String,Object> instanceVariables){
        ActInstanceMetaInfo metaInfo =null;
        if(instanceVariables==null)
        {
            return metaInfo;
        }
         Object metaInfoObj=instanceVariables.get(INSTANCE_VAR_METAINFO);
        if(metaInfoObj!=null){
            if(metaInfoObj instanceof ActInstanceMetaInfo)
            {
                return (ActInstanceMetaInfo)metaInfoObj;
            }else {
                try {
                    if (metaInfoObj instanceof ObjectNode) {
                        ObjectNode modelNode = (ObjectNode) metaInfoObj;
                         metaInfo = new ObjectMapper().readValue(modelNode.toString(), ActInstanceMetaInfo.class);
                    } else {
                         metaInfo = new ObjectMapper().readValue(metaInfoObj.toString(), ActInstanceMetaInfo.class);

                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    throw new IllegalArgumentException("读取METAINFO数据错误");
                }

            }
        }
        return metaInfo;
    }
}
