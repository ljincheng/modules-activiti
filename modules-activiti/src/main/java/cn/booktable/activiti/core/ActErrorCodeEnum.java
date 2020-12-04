package cn.booktable.activiti.core;

public enum ActErrorCodeEnum {
    OTHER("其他",10000),
    EMPTY_TASKID("任务编码不能为空",10001),
    EMPTY_INSTANCECODE("审批实例编号不能为空",10002),
    EMPTY_APPROVALSTATUS("审批状态不能为空",10003),
    UNEXIST_ACTIVETASK("审批任务不能为空",10004),
    INVALID_APPROVALUSER("审批人无效",10005),
    INSTANCE_SUSPENDED("审批实例已挂起",10006),
    EXIST_INSTANCECODE("审批实例编号已存在",10007),
    EXCLUDE_APPROVALSTATUS("审批状态不能为空",10008);

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
