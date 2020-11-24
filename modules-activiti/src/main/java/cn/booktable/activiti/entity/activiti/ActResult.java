package cn.booktable.activiti.entity.activiti;

import lombok.Data;

@Data
public class ActResult<T> {
    private Integer code;
    private String errorCode;
    private String msg;
    private T data;


}
