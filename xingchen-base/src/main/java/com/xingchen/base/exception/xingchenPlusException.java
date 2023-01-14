package com.xingchen.base.exception;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/10 9:14
 */
public class xingchenPlusException extends RuntimeException {

    private String errMessage;

    public xingchenPlusException() {
        super();
    }

    public xingchenPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public String getErrMessage(){
        return errMessage;
    }

    public static void cast(String errMessage){
        throw new xingchenPlusException(errMessage);
    }
    public static void cast(CommonError commonError){
        throw new xingchenPlusException(commonError.getErrMessage());
    }
}
