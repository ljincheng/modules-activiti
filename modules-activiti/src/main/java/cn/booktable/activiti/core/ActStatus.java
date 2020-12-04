package cn.booktable.activiti.core;

/**
 * @author ljc
 */
public class ActStatus {



    /** 审批任务状态:开始 **/
    public static String START="START";
    /** 审批任务状态:进行中 **/
    public static String PENDING="PENDING";
    /** 审批任务状态:已同意 **/
    public static String APPROVED="APPROVED";
    /** 审批任务状态:已拒绝 **/
    public static String REJECTED="REJECTED";
    /** 审批任务状态:已转交 **/
    public static String TRANSFERRED="TRANSFERRED";
    /** 审批任务状态:已完成 **/
    public static String DONE="DONE";

    /**
     * 进行中
     * 表示当前审批实例还在流转中，没有最终结果
     **/
    public static String INSTANCE_PENDING="PENDING";

    /**
     * 已同意
     *表示当前审批实例已经被通过
     ***/
    public static String INSTANCE_APPROVED="APPROVED";

    /**
     * 已拒绝
     * 表示当前审批实例已经被拒绝
     **/
    public static String INSTANCE_REJECTED="REJECTED";

    /**
     * 已撤回
     * 表示当前审批实例已经被发起人撤回
     **/
    public static String INSTANCE_CANCELED="CANCELED";

    /**
     * 已删除
     * 表示当前审批实例正在流程中，但是由于管理员停用或删除了当前审批定义，导致该审批实例变为已删除状态
     **/
    public static String INSTANCE_DELETED="DELETED";
    /**
     * 已完成
     */
    public static String INSTANCE_DONE="DONE";
}
