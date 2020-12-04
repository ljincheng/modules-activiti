package cn.booktable.activiti.core;

/**
 * 异常
 */
public class ActException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code=null;

    public ActException(){
        super();
    }

    /**
     * 异常信息
     * @param msg
     */
    public ActException(String msg)
    {
        super(msg);
    }

    public ActException(Integer code ,String msg)
    {
        super(msg);
        this.setCode(code);
    }

    public ActException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ActException(Throwable cause) {
        super(cause);
    }

    /**
     * 错误编号
     * @param code
     */
    public void setCode(Integer code)
    {
        this.code=code;
    }

    /**
     * 错误编号
     * @return
     */
    public Integer getCode()
    {
        return this.code;
    }
}
