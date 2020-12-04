package cn.booktable.activiti.utils;

import cn.booktable.activiti.core.ActErrorCodeEnum;
import cn.booktable.activiti.core.ActException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class AssertUtils {

    public static void isBlank(String param, ActErrorCodeEnum error) {
        if (!StringUtils.isBlank(param)) {
            throw new ActException(error.code(),error.msg());
        }
    }

    /**
     * 值不能为空
     * @param param
     */
    public static void isNotBlank(String param, ActErrorCodeEnum error) {
        if (StringUtils.isEmpty(param)) {
            throw new ActException(error.code(),error.msg());
        }
    }


    public static void isNull(Object object, ActErrorCodeEnum error) {
        if (object != null) {
            throw new ActException(error.code(),error.msg());
        }
    }



    public static void notNull(Object object, ActErrorCodeEnum error) {
        if (object == null) {
            throw new ActException(error.code(),error.msg());
        }
    }

    public static void compareLargeZero(long object, ActErrorCodeEnum error) {
        if (object<=0) {
            throw new ActException(error.code(),error.msg());
        }
    }

    public static void isTrue(boolean expression, ActErrorCodeEnum error) {
        if (!expression) {
            throw new ActException(error.code(),error.msg());
        }
    }

    public static boolean hasIn(String str, ActErrorCodeEnum error,String ...strs){
        if(strs!=null){
            for(String s :strs){
                if(s.equals(str)){
                    return true;
                }
            }
        }
        throw new ActException(error.code(),error.msg());
    }

}
