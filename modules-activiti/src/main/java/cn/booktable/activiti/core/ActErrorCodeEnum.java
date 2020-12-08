package cn.booktable.activiti.core;

public enum ActErrorCodeEnum {
    OTHER("其他",10000),
    EMPTY_TASKID("任务编码不能为空",10001),
    EMPTY_INSTANCECODE("审批实例编号不能为空",10002),
    EMPTY_APPROVALCODE("流程编码不能为空",10003),
    EMPTY_USERID("用户不能为空",10004),
    EMPTY_INSTANCENAME("审批实例名称不能为空",10005),
    EMPTY_APPROVALSTATUS("审批状态不能为空",10006),
    UNEXIST_ACTIVETASK("审批任务不能为空",10007),
    INVALID_APPROVALUSER("审批人无效",10008),
    INSTANCE_SUSPENDED("审批实例已挂起",10009),
    EXIST_INSTANCECODE("审批实例编号已存在",10010),
    EXIST_APPROVALCODE("流程编码已存在",10011),
    EXCLUDE_APPROVALSTATUS("审批状态不能为空",10012),
    FAIL_INSTANCESTART("创建审批实例失败",10013),
    UNEXIST_MODEL("流程模型不存在",10014),
    NOTEMPTY_MODEL_INSTANCE("存在流程模型审批实例不为空",10015);

    private String msg;
    private int code;

    ActErrorCodeEnum(String msg,int code){
        this.msg = msg;
        this.code = code;
    }

    public String msg(){
        return this.msg;
    }

    public int code(){
        return this.code;
    }
}
