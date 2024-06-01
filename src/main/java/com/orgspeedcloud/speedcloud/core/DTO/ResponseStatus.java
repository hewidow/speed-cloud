package com.orgspeedcloud.speedcloud.core.DTO;

/**
 * 枚举类,枚举各种不同的网络返回状态
 * @author Chen
 */

public enum ResponseStatus {
    /**
     * 返回成功状态,未找到状态
     */
    Success(200,"SUCCESS"),
    Not_Found(404,"NOT_FOUND"),
    Error(500,"INTERNAL_FAIL"),
    Not_Authentication(401,"认证失败"),
    Banned(402,"封禁");

    final String msg;
    final Integer code;

    ResponseStatus(Integer code,String msg){
        this.msg = msg;
        this.code = code;
    }
    public String getMsg(){
        return msg;
    }
    public Integer getCode(){
        return code;
    }
}
